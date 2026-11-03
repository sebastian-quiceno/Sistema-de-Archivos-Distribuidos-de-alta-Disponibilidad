package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FolderMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
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
    private final FolderMapper folderMapper;
    private final UserMapper userMapper;

    public FolderRepository(
            SpringDataFolderRepository springDataFolderRepository,
            SpringDataUserRepository springDataUserRepository,
            FolderMapper folderMapper,
            UserMapper userMapper) {
        this.springDataFolderRepository = springDataFolderRepository;
        this.springDataUserRepository = springDataUserRepository;
        this.folderMapper = folderMapper;
        this.userMapper = userMapper;
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

