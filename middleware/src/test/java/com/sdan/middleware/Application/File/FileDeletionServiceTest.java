package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.NodeName;
import com.sdan.middleware.Domain.ValueObjects.RegionName;
import com.sdan.middleware.Domain.ValueObjects.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeletionServiceTest {

    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private ChunkStorageService chunkStorageService;

    private FileDeletionService fileDeletionService;

    private File file;
    private Node node1;
    private Node node2;
    private FileHash hash;
    private FileKey key;

    @BeforeEach
    void setUp() {
        fileDeletionService = new FileDeletionService(
                fileRepository,
                chunkRepository,
                chunkStorageService
        );

        Folder folder = Folder.create(null, null, new FolderName("Folder"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Text");
        file = File.create(folder, fileType, new FileName("sample.txt"), 100L, 1);

        node1 = Node.create(new NodeName("node-1"), new RegionName("us-east-1"), 1000L, 500L);
        node2 = Node.create(new NodeName("node-2"), new RegionName("us-east-2"), 1000L, 500L);

        hash = new FileHash("a".repeat(64));
        key = new FileKey("k".repeat(116));
    }

    @Test
    void testDeleteFile_nullFile_doesNothing() {
        assertDoesNotThrow(() -> fileDeletionService.deleteFile(null));
        verifyNoInteractions(fileRepository, chunkRepository, chunkStorageService);
    }

    @Test
    void testDeleteFile_deletesMetadataFirst_thenDeletesPhysicalChunks_inOrder() {
        Chunk originalChunk = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        Chunk replicaChunk = Chunk.create(file, node2, true, 100L, hash, key, new Version("zz2"), 1);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(originalChunk));
        when(chunkRepository.getReplicaNodes(originalChunk.getId())).thenReturn(List.of(replicaChunk));

        fileDeletionService.deleteFile(file);

        // Verify strict order: metadata must be deleted BEFORE attempting physical chunk deletion
        InOrder inOrder = inOrder(chunkRepository, fileRepository, chunkStorageService);

        // Metadata deletion first
        inOrder.verify(chunkRepository).deleteById(originalChunk.getId());
        inOrder.verify(chunkRepository).deleteById(replicaChunk.getId());
        inOrder.verify(fileRepository).deleteById(file.getId());

        // Storage deletion second
        inOrder.verify(chunkStorageService).deleteChunk(originalChunk);
        inOrder.verify(chunkStorageService).deleteChunk(replicaChunk);
    }

    @Test
    void testDeleteFile_storageExceptionOnOneNode_doesNotInterruptOtherNodesCleanup() {
        Chunk chunk1 = Chunk.create(file, node1, false, 50L, hash, key, new Version("zz"), 1);
        Chunk chunk2 = Chunk.create(file, node2, false, 50L, hash, key, new Version("zz"), 2);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(chunk1, chunk2));
        when(chunkRepository.getReplicaNodes(any(UUID.class))).thenReturn(Collections.emptyList());

        // Node 1 fails with storage exception
        doThrow(new RuntimeException("Node 1 timeout")).when(chunkStorageService).deleteChunk(chunk1);

        assertDoesNotThrow(() -> fileDeletionService.deleteFile(file));

        // Metadata was still deleted
        verify(chunkRepository).deleteById(chunk1.getId());
        verify(chunkRepository).deleteById(chunk2.getId());
        verify(fileRepository).deleteById(file.getId());

        // Node 2 was still attempted for deletion despite Node 1 failure
        verify(chunkStorageService).deleteChunk(chunk1);
        verify(chunkStorageService).deleteChunk(chunk2);
    }
}

