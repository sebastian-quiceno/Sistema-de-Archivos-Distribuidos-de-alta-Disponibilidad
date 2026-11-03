package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper {

    private final UserMapper userMapper;

    public FolderMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Folder toDomain(FolderEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = userMapper.toDomain(entity.getUser());
        Folder fatherFolder = entity.getFatherFolder() != null ? toDomainSimple(entity.getFatherFolder()) : null;

        return Folder.reCreate(
                entity.getFolderId(),
                user,
                fatherFolder,
                new FolderName(entity.getName()),
                entity.getDateCreated()
        );
    }

    private Folder toDomainSimple(FolderEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = userMapper.toDomain(entity.getUser());
        return Folder.reCreate(
                entity.getFolderId(),
                user,
                null,
                new FolderName(entity.getName()),
                entity.getDateCreated()
        );
    }

    public FolderEntity toEntity(Folder domain, UserEntity userEntity, FolderEntity fatherFolderEntity) {
        if (domain == null) {
            return null;
        }
        return new FolderEntity(
                domain.getFolderId(),
                userEntity,
                fatherFolderEntity,
                domain.getName() != null ? domain.getName().getValue() : null,
                domain.getCreationDate()
        );
    }
}

