package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FileMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FolderMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFolderRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class FolderRepository implements IFolderRepository {

    private final SpringDataFolderRepository springDataFolderRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataFileRepository springDataFileRepository;
    private final FolderMapper folderMapper;
    private final UserMapper userMapper;
    private final FileMapper fileMapper;

    public FolderRepository(
            SpringDataFolderRepository springDataFolderRepository,
            SpringDataUserRepository springDataUserRepository,
            SpringDataFileRepository springDataFileRepository,
            FolderMapper folderMapper,
            UserMapper userMapper,
            FileMapper fileMapper) {
        this.springDataFolderRepository = springDataFolderRepository;
        this.springDataUserRepository = springDataUserRepository;
        this.springDataFileRepository = springDataFileRepository;
        this.folderMapper = folderMapper;
        this.userMapper = userMapper;
        this.fileMapper = fileMapper;
    }

    @Override
    public Folder getById(UUID id) {
        if (id == null) {
            return null;
        }
        return springDataFolderRepository.findById(id).map(folderMapper::toDomain).orElse(null);
    }

    @Override
    public List<Folder> getRelatedFolders(UUID id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return springDataFolderRepository.findRelatedFolders(id).stream()
                .map(folderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Folder> getRelatedFoldersByUserId(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return springDataFolderRepository.findRelatedFoldersById(userId).stream()
                .map(folderMapper::toDomain)
                .toList();
    }

    @Override
    public List<File> getRelatedFiles(UUID id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return springDataFileRepository.findFilesByFolderId(id).stream()
                .map(fileMapper::toDomain)
                .toList();
    }

    @Override
    public List<File> getRelatedFileByUserId(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return springDataFileRepository.findFilesByUserId(userId).stream()
                .map(fileMapper::toDomain)
                .toList();
    }

    @Override
    public void save(Folder folder) {
        if (folder == null) {
            return;
        }

        UserEntity userEntity = null;
        if (folder.getUser() != null && folder.getUser().getId() != null) {
            userEntity = springDataUserRepository.findById(folder.getUser().getId())
                    .orElseGet(() -> springDataUserRepository.save(userMapper.toEntity(folder.getUser())));
        }

        FolderEntity fatherFolderEntity = null;
        if (folder.getFatherFolder() != null && folder.getFatherFolder().getFolderId() != null) {
            fatherFolderEntity = springDataFolderRepository.findById(folder.getFatherFolder().getFolderId())
                    .orElse(null);
        }

        FolderEntity entity = folderMapper.toEntity(folder, userEntity, fatherFolderEntity);
        springDataFolderRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataFolderRepository.deleteById(id);
    }
}

