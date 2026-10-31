package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.*;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.*;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Node.InsufficientNodesForReplicationException;
import com.sdan.middleware.Domain.Exceptions.Node.NoAvailableNodesException;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IFolderRepository folderRepository;
    @Mock
    private IFileTypeRepository fileTypeRepository;
    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private INodeRepository nodeRepository;
    @Mock
    private IStorageService storageService;
    @Mock
    private IFileHasher fileHasher;

    @Mock
    private FilePartitionService filePartitionService;
    @Mock
    private NodeAssignmentService nodeAssignmentService;
    @Mock
    private ChunkStorageService chunkStorageService;
    @Mock
    private ChunkReplicationService chunkReplicationService;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(
                fileRepository,
                folderRepository,
                fileTypeRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService
        );
    }

    @Test
    void testFilePartitionService_calculateTotalPartitions() {
        FilePartitionService partitionService = new FilePartitionService();
        assertEquals(0, partitionService.calculateTotalPartitions(0));
        assertEquals(1, partitionService.calculateTotalPartitions(1024));
        assertEquals(1, partitionService.calculateTotalPartitions(32 * 1024 * 1024));
        assertEquals(2, partitionService.calculateTotalPartitions(32 * 1024 * 1024 + 1));
        assertThrows(IllegalArgumentException.class, () -> partitionService.calculateTotalPartitions(-1));
    }

    @Test
    void testNodeAssignmentService_emptyNodes_throwsException() {
        when(nodeRepository.getIdsOnline()).thenReturn(Collections.emptyList());
        NodeAssignmentService assignmentService = new NodeAssignmentService(nodeRepository);
        assertThrows(NoAvailableNodesException.class, () -> assignmentService.getRandomNodeAssignments(1));
    }

    @Test
    void testNodeAssignmentService_insufficientNodesForReplication_throwsException() {
        UUID node1 = UUID.randomUUID();
        when(nodeRepository.getIdsOnline()).thenReturn(List.of(node1)); // Need at least 2 for factor 3
        NodeAssignmentService assignmentService = new NodeAssignmentService(nodeRepository);
        assertThrows(InsufficientNodesForReplicationException.class, () -> assignmentService.getDifferentsNodesIds(node1));
    }

    @Test
    void testFileService_validateCommand_nullCommand() {
        assertThrows(IllegalArgumentException.class, () -> fileService.partitionFile(null));
    }

    @Test
    void testFileService_validateCommand_negativeSize() {
        CreateFileCommand cmd = new CreateFileCommand(UUID.randomUUID(), UUID.randomUUID(), "test.txt", mock(InputStream.class), -5L);
        assertThrows(IllegalArgumentException.class, () -> fileService.partitionFile(cmd));
    }

    @Test
    void testFileService_validateCommand_nullFileStream() {
        CreateFileCommand cmd = new CreateFileCommand(UUID.randomUUID(), UUID.randomUUID(), "test.txt", null, 100L);
        assertThrows(IllegalArgumentException.class, () -> fileService.partitionFile(cmd));
    }

    @Test
    void testFileService_fileTypeNotFound() {
        UUID typeId = UUID.randomUUID();
        CreateFileCommand cmd = new CreateFileCommand(UUID.randomUUID(), typeId, "test.txt", new ByteArrayInputStream(new byte[10]), 10L);
        when(fileTypeRepository.getById(typeId)).thenReturn(Optional.empty());

        assertThrows(FileTypeNotFoundException.class, () -> fileService.partitionFile(cmd));
    }

    @Test
    void testFileService_folderNotFound() {
        UUID folderId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        FileType mockFileType = FileType.create(new FileTypeName("text/plain"), "document");
        when(fileTypeRepository.getById(typeId)).thenReturn(Optional.of(mockFileType));
        when(folderRepository.getById(folderId)).thenReturn(null);

        CreateFileCommand cmd = new CreateFileCommand(folderId, typeId, "test.txt", new ByteArrayInputStream(new byte[10]), 10L);

        assertThrows(FolderNotFoundException.class, () -> fileService.partitionFile(cmd));
    }

    @Test
    void testFileService_successfulCoordination() {
        UUID folderId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        FileType mockFileType = FileType.create(new FileTypeName("text/plain"), "document");
        Folder mockFolder = mock(Folder.class);

        when(fileTypeRepository.getById(typeId)).thenReturn(Optional.of(mockFileType));
        when(folderRepository.getById(folderId)).thenReturn(mockFolder);

        when(filePartitionService.calculateTotalPartitions(anyLong())).thenReturn(1);
        UUID nodeId = UUID.randomUUID();
        Queue<UUID> nodeQueue = new ArrayDeque<>(List.of(nodeId));
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(nodeQueue);

        Chunk mockChunk = mock(Chunk.class);
        doAnswer(invocation -> {
            Consumer<byte[]> consumer = invocation.getArgument(2);
            consumer.accept(new byte[]{1, 2, 3});
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), anyLong(), any());

        when(chunkStorageService.uploadToNode(eq(nodeId), any(File.class), any(byte[].class), eq(false))).thenReturn(mockChunk);

        byte[] content = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        CreateFileCommand cmd = new CreateFileCommand(folderId, typeId, "test.txt", new ByteArrayInputStream(content), (long) content.length);

        fileService.partitionFile(cmd);

        verify(filePartitionService).calculateTotalPartitions((long) content.length);
        verify(nodeAssignmentService).getRandomNodeAssignments(1);
        verify(chunkStorageService).uploadToNode(eq(nodeId), any(File.class), any(byte[].class), eq(false));
        verify(chunkReplicationService).replicateFile(eq(nodeId), eq(mockChunk));
        verify(fileRepository).save(any(File.class));
        verify(chunkRepository).save(mockChunk);
    }
}
