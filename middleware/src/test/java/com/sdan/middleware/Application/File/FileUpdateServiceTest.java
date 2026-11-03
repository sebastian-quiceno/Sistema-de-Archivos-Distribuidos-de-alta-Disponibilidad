package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import com.sdan.middleware.Domain.ValueObjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUpdateServiceTest {

    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private FilePartitionService filePartitionService;
    @Mock
    private NodeAssignmentService nodeAssignmentService;
    @Mock
    private ChunkStorageService chunkStorageService;
    @Mock
    private ChunkReplicationService chunkReplicationService;

    private FileUpdateService fileUpdateService;

    private File file;
    private Node node1;
    private Node node2;
    private FileHash hash;
    private FileKey key;

    @BeforeEach
    void setUp() {
        fileUpdateService = new FileUpdateService(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService
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
    void testUpdateFile_nullFile_throwsIllegalArgumentException() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2});
        assertThrows(IllegalArgumentException.class, () -> fileUpdateService.updateFile(null, stream, 2L));
    }

    @Test
    void testUpdateFile_nullStream_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> fileUpdateService.updateFile(file, null, 2L));
    }

    @Test
    void testUpdateFile_nullOrNegativeSize_throwsIllegalArgumentException() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2});
        assertThrows(IllegalArgumentException.class, () -> fileUpdateService.updateFile(file, stream, null));
        assertThrows(IllegalArgumentException.class, () -> fileUpdateService.updateFile(file, stream, -1L));
    }

    @Test
    void testUpdateFile_success_uploadsNewChunks_verifies_persistsMetadata_deletesOldChunks() {
        // Setup old chunks
        Chunk oldOriginal = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        Chunk oldReplica = Chunk.create(file, node2, true, 100L, hash, key, new Version("zz2"), 1);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(oldOriginal));
        when(chunkRepository.getReplicaNodes(oldOriginal.getId())).thenReturn(List.of(oldReplica));

        // Setup new chunks
        long newSize = 50L;
        when(filePartitionService.calculateTotalPartitions(newSize)).thenReturn(1);
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(new ArrayDeque<>(List.of(node1.getId())));

        Chunk newOriginal = Chunk.create(file, node1, false, newSize, hash, key, new Version("zz"), 1);
        Chunk newReplica = Chunk.create(file, node2, true, newSize, hash, key, new Version("zz2"), 1);

        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            consumer.accept(new byte[50]);
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(newSize), any());

        when(chunkStorageService.uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1)))
                .thenReturn(newOriginal);
        when(chunkReplicationService.replicateFile(newOriginal, ChunkReplicationService.REPLICATION_FACTOR))
                .thenReturn(List.of(newReplica));

        // Verification succeeds
        when(chunkStorageService.getChunk(newOriginal))
                .thenReturn(new ChunkGetMethodOutput(newOriginal.getId(), new ByteArrayInputStream(new byte[50]), false));
        when(chunkStorageService.getChunk(newReplica))
                .thenReturn(new ChunkGetMethodOutput(newReplica.getId(), new ByteArrayInputStream(new byte[50]), false));

        InputStream stream = new ByteArrayInputStream(new byte[50]);
        fileUpdateService.updateFile(file, stream, newSize);

        // Verify order of execution:
        // 1. Upload & replication
        // 2. Verification
        // 3. Metadata persistence
        // 4. Old chunks deletion
        InOrder inOrder = inOrder(chunkStorageService, chunkReplicationService, chunkRepository, fileRepository);

        inOrder.verify(chunkStorageService).uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1));
        inOrder.verify(chunkReplicationService).replicateFile(newOriginal, ChunkReplicationService.REPLICATION_FACTOR);
        inOrder.verify(chunkStorageService).getChunk(newOriginal);
        inOrder.verify(chunkStorageService).getChunk(newReplica);

        inOrder.verify(chunkRepository).save(newOriginal);
        inOrder.verify(chunkRepository).save(newReplica);
        inOrder.verify(fileRepository).save(file);

        inOrder.verify(chunkRepository).deleteById(oldOriginal.getId());
        inOrder.verify(chunkRepository).deleteById(oldReplica.getId());
        inOrder.verify(chunkStorageService).deleteChunk(oldOriginal);
        inOrder.verify(chunkStorageService).deleteChunk(oldReplica);

        // Verify updated file state
        assertEquals(50L, file.getBytesSize());
        assertEquals(1, file.getTotalPartitions());
    }

    @Test
    void testUpdateFile_failureDuringUpload_rollbacksUploadedChunks_preservesOldVersion() {
        Chunk oldOriginal = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(oldOriginal));
        when(chunkRepository.getReplicaNodes(oldOriginal.getId())).thenReturn(Collections.emptyList());

        long newSize = 100L;
        when(filePartitionService.calculateTotalPartitions(newSize)).thenReturn(2);
        when(nodeAssignmentService.getRandomNodeAssignments(2)).thenReturn(new ArrayDeque<>(List.of(node1.getId(), node2.getId())));

        Chunk newChunk1 = Chunk.create(file, node1, false, 50L, hash, key, new Version("zz"), 1);

        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            // First chunk succeeds
            consumer.accept(new byte[50]);
            // Second chunk fails
            throw new FileProcessingException("Network error on node 2");
        }).when(filePartitionService).partition(any(InputStream.class), eq(newSize), any());

        when(chunkStorageService.uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1)))
                .thenReturn(newChunk1);
        when(chunkReplicationService.replicateFile(newChunk1, ChunkReplicationService.REPLICATION_FACTOR))
                .thenReturn(Collections.emptyList());

        InputStream stream = new ByteArrayInputStream(new byte[100]);

        assertThrows(FileProcessingException.class, () -> fileUpdateService.updateFile(file, stream, newSize));

        // Verify rollback: partial new chunk is deleted from storage
        verify(chunkStorageService).deleteChunk(newChunk1);

        // Verify metadata was NOT saved
        verify(fileRepository, never()).save(any());
        verify(chunkRepository, never()).save(any());

        // Verify old chunk was NOT deleted
        verify(chunkRepository, never()).deleteById(oldOriginal.getId());
        verify(chunkStorageService, never()).deleteChunk(oldOriginal);
    }

    @Test
    void testUpdateFile_failureDuringVerification_rollbacksNewChunks_preservesOldVersion() {
        Chunk oldOriginal = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(oldOriginal));
        when(chunkRepository.getReplicaNodes(oldOriginal.getId())).thenReturn(Collections.emptyList());

        long newSize = 50L;
        when(filePartitionService.calculateTotalPartitions(newSize)).thenReturn(1);
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(new ArrayDeque<>(List.of(node1.getId())));

        Chunk newOriginal = Chunk.create(file, node1, false, newSize, hash, key, new Version("zz"), 1);

        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            consumer.accept(new byte[50]);
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(newSize), any());

        when(chunkStorageService.uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1)))
                .thenReturn(newOriginal);
        when(chunkReplicationService.replicateFile(newOriginal, ChunkReplicationService.REPLICATION_FACTOR))
                .thenReturn(Collections.emptyList());

        // Verification returns corrupted chunk
        when(chunkStorageService.getChunk(newOriginal))
                .thenReturn(new ChunkGetMethodOutput(newOriginal.getId(), null, true));

        InputStream stream = new ByteArrayInputStream(new byte[50]);

        assertThrows(FileProcessingException.class, () -> fileUpdateService.updateFile(file, stream, newSize));

        // Rollback: new chunk cleaned up
        verify(chunkStorageService).deleteChunk(newOriginal);

        // Old version untouched
        verify(fileRepository, never()).save(any());
        verify(chunkRepository, never()).deleteById(oldOriginal.getId());
    }

    @Test
    void testUpdateFile_failureDuringMetadataSave_rollbacksNewChunksAndDb_preservesOldVersion() {
        Chunk oldOriginal = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(oldOriginal));
        when(chunkRepository.getReplicaNodes(oldOriginal.getId())).thenReturn(Collections.emptyList());

        long newSize = 50L;
        when(filePartitionService.calculateTotalPartitions(newSize)).thenReturn(1);
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(new ArrayDeque<>(List.of(node1.getId())));

        Chunk newOriginal = Chunk.create(file, node1, false, newSize, hash, key, new Version("zz"), 1);

        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            consumer.accept(new byte[50]);
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(newSize), any());

        when(chunkStorageService.uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1)))
                .thenReturn(newOriginal);
        when(chunkReplicationService.replicateFile(newOriginal, ChunkReplicationService.REPLICATION_FACTOR))
                .thenReturn(Collections.emptyList());

        when(chunkStorageService.getChunk(newOriginal))
                .thenReturn(new ChunkGetMethodOutput(newOriginal.getId(), new ByteArrayInputStream(new byte[50]), false));

        // DB save fails
        doThrow(new RuntimeException("DB down")).when(fileRepository).save(file);

        InputStream stream = new ByteArrayInputStream(new byte[50]);

        assertThrows(RuntimeException.class, () -> fileUpdateService.updateFile(file, stream, newSize));

        // Saved new chunks cleaned up from DB
        verify(chunkRepository).deleteById(newOriginal.getId());

        // New chunks cleaned up from storage
        verify(chunkStorageService).deleteChunk(newOriginal);

        // Old version not deleted
        verify(chunkRepository, never()).deleteById(oldOriginal.getId());
        verify(chunkStorageService, never()).deleteChunk(oldOriginal);
    }

    @Test
    void testUpdateFile_failureDuringOldChunksCleanup_doesNotFailOperation() {
        Chunk oldOriginal = Chunk.create(file, node1, false, 100L, hash, key, new Version("zz"), 1);
        Chunk oldReplica = Chunk.create(file, node2, true, 100L, hash, key, new Version("zz2"), 1);

        when(chunkRepository.getFileChunks(file.getId())).thenReturn(List.of(oldOriginal));
        when(chunkRepository.getReplicaNodes(oldOriginal.getId())).thenReturn(List.of(oldReplica));

        long newSize = 50L;
        when(filePartitionService.calculateTotalPartitions(newSize)).thenReturn(1);
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(new ArrayDeque<>(List.of(node1.getId())));

        Chunk newOriginal = Chunk.create(file, node1, false, newSize, hash, key, new Version("zz"), 1);

        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            consumer.accept(new byte[50]);
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(newSize), any());

        when(chunkStorageService.uploadToNode(eq(node1.getId()), eq(file), any(byte[].class), eq(false), eq(1)))
                .thenReturn(newOriginal);
        when(chunkReplicationService.replicateFile(newOriginal, ChunkReplicationService.REPLICATION_FACTOR))
                .thenReturn(Collections.emptyList());

        when(chunkStorageService.getChunk(newOriginal))
                .thenReturn(new ChunkGetMethodOutput(newOriginal.getId(), new ByteArrayInputStream(new byte[50]), false));

        // Old chunk physical deletion fails for node1
        doThrow(new RuntimeException("Node 1 down")).when(chunkStorageService).deleteChunk(oldOriginal);

        InputStream stream = new ByteArrayInputStream(new byte[50]);

        // Operation must still complete without throwing!
        assertDoesNotThrow(() -> fileUpdateService.updateFile(file, stream, newSize));

        // New version metadata and chunks are saved
        verify(fileRepository).save(file);
        verify(chunkRepository).save(newOriginal);

        // Old replica deletion still attempted
        verify(chunkStorageService).deleteChunk(oldReplica);
    }
}

