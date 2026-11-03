package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.File.FileIsCorruptedException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileReconstructionServiceTest {

    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private IFileRepository fileRepository;
    @Mock
    private ChunkStorageService chunkStorageService;

    private FileReconstructionService fileReconstructionService;

    private File file;
    private Node node1;
    private FileHash hash;
    private FileKey key;

    @BeforeEach
    void setUp() {
        fileReconstructionService = new FileReconstructionService(
                chunkRepository,
                fileRepository,
                chunkStorageService
        );

        Folder folder = Folder.create(null, null, new FolderName("Folder"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Text");
        file = File.create(folder, fileType, new FileName("doc.txt"), 20L, 2);

        node1 = Node.create(new NodeName("node-1"), new RegionName("us-east-1"), 1000L, 500L);
        hash = new FileHash("a".repeat(64));
        key = new FileKey("k".repeat(116));
    }

    @Test
    void testMergeFile_sortsChunksByPartNumberAscending_andReturnsFullContent() throws IOException {
        // Create chunk 2 and chunk 1 passed in reverse order [chunk2, chunk1]
        Chunk chunk1 = Chunk.create(file, node1, false, 10L, hash, key, new Version("zz"), 1);
        Chunk chunk2 = Chunk.create(file, node1, false, 10L, hash, key, new Version("zz"), 2);

        when(fileRepository.getOriginalsChunks(file.getId())).thenReturn(List.of(chunk2, chunk1));

        when(chunkStorageService.getChunk(chunk1)).thenReturn(
                new ChunkGetMethodOutput(chunk1.getId(), new ByteArrayInputStream("HELLO_".getBytes(StandardCharsets.UTF_8)), false)
        );
        when(chunkStorageService.getChunk(chunk2)).thenReturn(
                new ChunkGetMethodOutput(chunk2.getId(), new ByteArrayInputStream("WORLD!".getBytes(StandardCharsets.UTF_8)), false)
        );

        InputStream mergedStream = fileReconstructionService.mergeFile(file);

        assertNotNull(mergedStream);
        byte[] mergedBytes = mergedStream.readAllBytes();
        String resultText = new String(mergedBytes, StandardCharsets.UTF_8);

        // Crucial test: partNumber 1 ("HELLO_") must come before partNumber 2 ("WORLD!")
        assertEquals("HELLO_WORLD!", resultText);
    }

    @Test
    void testMergeFile_primaryCorrupted_fallsBackToReplica() throws IOException {
        Chunk chunk1 = Chunk.create(file, node1, false, 10L, hash, key, new Version("zz"), 1);
        Chunk replica1 = Chunk.create(file, node1, true, 10L, hash, key, new Version("zz2"), 1);

        when(fileRepository.getOriginalsChunks(file.getId())).thenReturn(List.of(chunk1));

        // Primary returns corrupted
        when(chunkStorageService.getChunk(chunk1)).thenReturn(
                new ChunkGetMethodOutput(chunk1.getId(), null, true)
        );
        // Replica returns sane
        when(chunkRepository.getReplicaNodes(chunk1.getId())).thenReturn(List.of(replica1));
        when(chunkStorageService.getChunk(replica1)).thenReturn(
                new ChunkGetMethodOutput(replica1.getId(), new ByteArrayInputStream("SANE_DATA".getBytes(StandardCharsets.UTF_8)), false)
        );

        InputStream mergedStream = fileReconstructionService.mergeFile(file);

        assertNotNull(mergedStream);
        byte[] mergedBytes = mergedStream.readAllBytes();
        assertEquals("SANE_DATA", new String(mergedBytes, StandardCharsets.UTF_8));
    }

    @Test
    void testMergeFile_allCorrupted_throwsFileIsCorruptedException() {
        Chunk chunk1 = Chunk.create(file, node1, false, 10L, hash, key, new Version("zz"), 1);
        Chunk replica1 = Chunk.create(file, node1, true, 10L, hash, key, new Version("zz2"), 1);

        when(fileRepository.getOriginalsChunks(file.getId())).thenReturn(List.of(chunk1));
        when(chunkStorageService.getChunk(chunk1)).thenReturn(new ChunkGetMethodOutput(chunk1.getId(), null, true));
        when(chunkRepository.getReplicaNodes(chunk1.getId())).thenReturn(List.of(replica1));
        when(chunkStorageService.getChunk(replica1)).thenReturn(new ChunkGetMethodOutput(replica1.getId(), null, true));

        assertThrows(FileIsCorruptedException.class, () -> fileReconstructionService.mergeFile(file));
    }
}