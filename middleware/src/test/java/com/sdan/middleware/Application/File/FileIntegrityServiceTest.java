package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
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
import com.sdan.middleware.BuildingBlocks.Application.FileAuditStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileIntegrityServiceTest {

    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private IStorageService storageService;
    @Mock
    private IFileHasher fileHasher;
    @Mock
    private ChunkReplicationService chunkReplicationService;

    private FileIntegrityService fileIntegrityService;
    private File file;
    private Node nodePrimary;
    private Node nodeReplica;
    private FileHash hash;
    private FileKey key;

    @BeforeEach
    void setUp() {
        fileIntegrityService = new FileIntegrityService(
                chunkRepository,
                storageService,
                fileHasher,
                chunkReplicationService
        );

        Folder folder = Folder.create(null, null, new FolderName("Folder"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Text");
        file = File.create(folder, fileType, new FileName("test.txt"), 100L, 1);

        nodePrimary = Node.create(new NodeName("node-1"), new RegionName("us-east-1"), 1000L, 500L);
        nodeReplica = Node.create(new NodeName("node-2"), new RegionName("us-east-2"), 1000L, 500L);

        hash = new FileHash("a".repeat(64));
        key = new FileKey("k".repeat(116));
    }

    @Test
    void testVerifyAndHealFile_allHealthy_returnsHealthy() {
        Chunk chunk = Chunk.create(file, nodePrimary, false, 100L, hash, key, new Version("zz"), 1);
        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(chunk));
        when(chunkRepository.getReplicaNodes(chunk.getId())).thenReturn(Collections.emptyList());

        when(storageService.getObject(anyString(), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(fileHasher.Verify(any(InputStream.class), eq(hash.getValue()))).thenReturn(true);

        FileAuditStatus status = fileIntegrityService.verifyAndHealFile(file);

        assertEquals(FileAuditStatus.HEALTHY, status);
        verify(chunkReplicationService, never()).healChunk(any(), any());
    }

    @Test
    void testVerifyAndHealFile_primaryCorrupted_healedUsingReplica_andOriginalIsIncludedInHealing() {
        Chunk primary = Chunk.create(file, nodePrimary, false, 100L, hash, key, new Version("zz"), 1);
        Chunk replica = Chunk.create(file, nodeReplica, true, 100L, hash, key, new Version("zz2"), 1);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(primary));
        when(chunkRepository.getReplicaNodes(primary.getId())).thenReturn(List.of(replica));

        // Primary returns corrupted (hash verify = false)
        // Replica returns healthy (hash verify = true)
        when(storageService.getObject(eq(nodePrimary.getName().getValue()), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream("corrupted data".getBytes()));
        when(storageService.getObject(eq(nodeReplica.getName().getValue()), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream("sane data".getBytes()));

        when(fileHasher.Verify(any(InputStream.class), eq(hash.getValue())))
                .thenReturn(false) // for primary
                .thenReturn(true);  // for replica

        FileAuditStatus status = fileIntegrityService.verifyAndHealFile(file);

        assertEquals(FileAuditStatus.HEALED, status);
        // CRITICAL CHECK: The primary corrupted chunk MUST be passed to healChunk!
        verify(chunkReplicationService, times(1)).healChunk(eq(primary), eq(replica));
    }

    @Test
    void testVerifyAndHealFile_allCorrupted_returnsUnrecoverable() {
        Chunk primary = Chunk.create(file, nodePrimary, false, 100L, hash, key, new Version("zz"), 1);
        Chunk replica = Chunk.create(file, nodeReplica, true, 100L, hash, key, new Version("zz2"), 1);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(primary));
        when(chunkRepository.getReplicaNodes(primary.getId())).thenReturn(List.of(replica));

        when(storageService.getObject(anyString(), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream("bad1".getBytes()))
                .thenReturn(new ByteArrayInputStream("bad2".getBytes()));
        when(fileHasher.Verify(any(InputStream.class), eq(hash.getValue()))).thenReturn(false);

        FileAuditStatus status = fileIntegrityService.verifyAndHealFile(file);

        assertEquals(FileAuditStatus.UNRECOVERABLE, status);
        verify(chunkReplicationService, never()).healChunk(any(), any());
    }
}
