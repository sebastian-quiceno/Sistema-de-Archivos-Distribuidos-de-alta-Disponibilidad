package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FolderMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
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

    private UserMapper userMapper;
    private FolderMapper folderMapper;
    private FolderRepository folderRepository;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        folderMapper = new FolderMapper(userMapper);
        folderRepository = new FolderRepository(
                springDataFolderRepository,
                springDataUserRepository,
                folderMapper,
                userMapper
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
    void testSave() {
        User user = User.create(new UserName("jane"), new PasswordHash("pwd"));
        Folder folder = Folder.create(user, null, new FolderName("Photos"));

        when(springDataUserRepository.findById(any())).thenReturn(Optional.of(userMapper.toEntity(user)));

        folderRepository.save(folder);

        verify(springDataFolderRepository, times(1)).save(any(FolderEntity.class));
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        folderRepository.deleteById(id);

        verify(springDataFolderRepository, times(1)).deleteById(id);
    }
}

