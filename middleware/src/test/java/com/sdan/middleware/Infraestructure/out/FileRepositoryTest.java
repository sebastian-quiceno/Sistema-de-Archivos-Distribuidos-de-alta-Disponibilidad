package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.ValueObjects.*;
import com.sdan.middleware.Infraestructure.out.Entities.*;
import com.sdan.middleware.Infraestructure.out.Mappers.*;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataChunkRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileTypeRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileRepositoryTest {

    @Mock
    private SpringDataFileRepository springDataFileRepository;
    @Mock
    private SpringDataFolderRepository springDataFolderRepository;
    @Mock
    private SpringDataFileTypeRepository springDataFileTypeRepository;
    @Mock
    private SpringDataChunkRepository springDataChunkRepository;

    private UserMapper userMapper;
    private FolderMapper folderMapper;
    private FileTypeMapper fileTypeMapper;
    private NodeMapper nodeMapper;
    private FileMapper fileMapper;
    private ChunkMapper chunkMapper;
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        folderMapper = new FolderMapper(userMapper);
        fileTypeMapper = new FileTypeMapper();
        nodeMapper = new NodeMapper();
        fileMapper = new FileMapper(folderMapper, fileTypeMapper);
        chunkMapper = new ChunkMapper(fileMapper, nodeMapper);

        fileRepository = new FileRepository(
                springDataFileRepository,
                springDataFolderRepository,
                springDataFileTypeRepository,
                springDataChunkRepository,
                fileMapper,
                folderMapper,
                fileTypeMapper,
                chunkMapper
        );
    }

    @Test
    void testGetById_found() {
        UUID fileId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "text/plain", "Text");
        FileEntity fileEntity = new FileEntity(fileId, folderEntity, fileTypeEntity, "notes.txt", 100L, new Date(), new Date(), 1);

        when(springDataFileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        Optional<File> result = fileRepository.getById(fileId);

        assertTrue(result.isPresent());
        assertEquals("notes.txt", result.get().getName().getValue());
    }

    @Test
    void testGetAll() {
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "text/plain", "Text");
        FileEntity fileEntity = new FileEntity(UUID.randomUUID(), folderEntity, fileTypeEntity, "notes.txt", 100L, new Date(), new Date(), 1);

        when(springDataFileRepository.findAll()).thenReturn(List.of(fileEntity));

        List<File> all = fileRepository.getAll();

        assertEquals(1, all.size());
    }

    @Test
    void testFindPage() {
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "text/plain", "Text");
        FileEntity fileEntity = new FileEntity(UUID.randomUUID(), folderEntity, fileTypeEntity, "notes.txt", 100L, new Date(), new Date(), 1);

        when(springDataFileRepository.findPagedFiles(any(Pageable.class))).thenReturn(List.of(fileEntity));

        List<File> page = fileRepository.findPage(0, 10);

        assertEquals(1, page.size());
    }

    @Test
    void testGetOriginalsChunks() {
        UUID fileId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "text/plain", "Text");
        FileEntity fileEntity = new FileEntity(fileId, folderEntity, fileTypeEntity, "notes.txt", 100L, new Date(), new Date(), 1);
        StatusEntity statusEntity = new StatusEntity(UUID.randomUUID(), "ONLINE", "Online");
        NodeEntity nodeEntity = new NodeEntity(UUID.randomUUID(), statusEntity, "node-1", "us-east-1", 1000L, 500L, new Date());

        ChunkEntity chunkEntity = new ChunkEntity(
                UUID.randomUUID(),
                fileEntity,
                nodeEntity,
                1,
                false,
                100L,
                "a".repeat(64),
                "k".repeat(116),
                "zz"
        );

        when(springDataChunkRepository.findOriginalChunksByFileId(fileId)).thenReturn(List.of(chunkEntity));

        List<Chunk> chunks = fileRepository.getOriginalsChunks(fileId);

        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).getPartNumber());
        assertFalse(chunks.get(0).getReplica());
    }

    @Test
    void testSave() {
        Folder folder = Folder.create(null, null, new FolderName("Folder"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Text");
        File file = File.create(folder, fileType, new FileName("sample.txt"), 100L, 1);

        fileRepository.save(file);

        verify(springDataFileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        fileRepository.deleteById(id);

        verify(springDataFileRepository, times(1)).deleteById(id);
    }
}

