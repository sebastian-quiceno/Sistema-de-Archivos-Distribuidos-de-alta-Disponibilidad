package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FileMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FileTypeMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FolderMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFolderRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderRepositoryTest {

    @Mock
    private SpringDataFolderRepository springDataFolderRepository;
    @Mock
    private SpringDataUserRepository springDataUserRepository;
    @Mock
    private SpringDataFileRepository springDataFileRepository;

    private UserMapper userMapper;
    private FolderMapper folderMapper;
    private FileTypeMapper fileTypeMapper;
    private FileMapper fileMapper;
    private FolderRepository folderRepository;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        folderMapper = new FolderMapper(userMapper);
        fileTypeMapper = new FileTypeMapper();
        fileMapper = new FileMapper(folderMapper, fileTypeMapper);
        folderRepository = new FolderRepository(
                springDataFolderRepository,
                springDataUserRepository,
                springDataFileRepository,
                folderMapper,
                userMapper,
                fileMapper
        );
    }

    @Test
    void testGetById_found() {
        UUID folderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity entity = new FolderEntity(folderId, userEntity, null, "Documents", new Date());

        when(springDataFolderRepository.findById(folderId)).thenReturn(Optional.of(entity));

        Folder result = folderRepository.getById(folderId);

        assertNotNull(result);
        assertEquals("Documents", result.getName().getValue());
    }

    @Test
    void testGetById_null() {
        assertNull(folderRepository.getById(null));
    }

    @Test
    void testGetRelatedFolders() {
        UUID parentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity child = new FolderEntity(UUID.randomUUID(), userEntity, null, "SubFolder", new Date());

        when(springDataFolderRepository.findRelatedFolders(parentId)).thenReturn(List.of(child));

        List<Folder> related = folderRepository.getRelatedFolders(parentId);

        assertEquals(1, related.size());
        assertEquals("SubFolder", related.get(0).getName().getValue());
    }

    @Test
    void testGetRelatedFolders_nullId() {
        List<Folder> related = folderRepository.getRelatedFolders(null);
        assertTrue(related.isEmpty());
    }

    @Test
    void testGetRelatedFoldersByUserId() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity rootFolder = new FolderEntity(UUID.randomUUID(), userEntity, null, "RootFolder", new Date());

        when(springDataFolderRepository.findRelatedFoldersById(userId)).thenReturn(List.of(rootFolder));

        List<Folder> folders = folderRepository.getRelatedFoldersByUserId(userId);

        assertEquals(1, folders.size());
        assertEquals("RootFolder", folders.get(0).getName().getValue());
    }

    @Test
    void testGetRelatedFoldersByUserId_nullUserId() {
        List<Folder> folders = folderRepository.getRelatedFoldersByUserId(null);
        assertTrue(folders.isEmpty());
    }

    @Test
    void testGetRootFolderByUserId_found() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity rootFolder = new FolderEntity(UUID.randomUUID(), userEntity, null, "root", new Date());

        when(springDataFolderRepository.findRootFoldersByUserId(userId)).thenReturn(List.of(rootFolder));

        Optional<Folder> result = folderRepository.getRootFolderByUserId(userId);

        assertTrue(result.isPresent());
        assertEquals("root", result.get().getName().getValue());
    }

    @Test
    void testGetRootFolderByUserId_notFound() {
        UUID userId = UUID.randomUUID();
        when(springDataFolderRepository.findRootFoldersByUserId(userId)).thenReturn(List.of());

        Optional<Folder> result = folderRepository.getRootFolderByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRootFolderByUserId_nullUserId() {
        Optional<Folder> result = folderRepository.getRootFolderByUserId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRelatedFiles() {
        UUID folderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(folderId, userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "pdf", "application/pdf");
        FileEntity fileEntity = new FileEntity(UUID.randomUUID(), folderEntity, fileTypeEntity, "report.pdf", 1024L, new Date(), new Date(), 1);

        when(springDataFileRepository.findFilesByFolderId(folderId)).thenReturn(List.of(fileEntity));

        List<File> files = folderRepository.getRelatedFiles(folderId);

        assertEquals(1, files.size());
        assertEquals("report.pdf", files.get(0).getName().getValue());
    }

    @Test
    void testGetRelatedFiles_nullFolderId() {
        List<File> files = folderRepository.getRelatedFiles(null);
        assertTrue(files.isEmpty());
    }

    @Test
    void testGetRelatedFileByUserId() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "txt", "text/plain");
        FileEntity fileEntity = new FileEntity(UUID.randomUUID(), folderEntity, fileTypeEntity, "notes.txt", 512L, new Date(), new Date(), 1);

        when(springDataFileRepository.findFilesByUserId(userId)).thenReturn(List.of(fileEntity));

        List<File> files = folderRepository.getRelatedFileByUserId(userId);

        assertEquals(1, files.size());
        assertEquals("notes.txt", files.get(0).getName().getValue());
    }

    @Test
    void testGetRelatedFileByUserId_nullUserId() {
        List<File> files = folderRepository.getRelatedFileByUserId(null);
        assertTrue(files.isEmpty());
    }

    @Test
    void testSave() {
        User user = User.create(new UserName("jane"), new PasswordHash("pwd"));
        Folder folder = Folder.create(user, null, new FolderName("Photos"));

        when(springDataUserRepository.findById(any())).thenReturn(Optional.of(userMapper.toEntity(user)));

        folderRepository.save(folder);

        verify(springDataFolderRepository, times(1)).save(any(FolderEntity.class));
    }

    @Test
    void testSave_nullFolder() {
        folderRepository.save(null);
        verify(springDataFolderRepository, never()).save(any());
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        folderRepository.deleteById(id);

        verify(springDataFolderRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteById_nullId() {
        folderRepository.deleteById(null);
        verify(springDataFolderRepository, never()).deleteById(any());
    }
}

