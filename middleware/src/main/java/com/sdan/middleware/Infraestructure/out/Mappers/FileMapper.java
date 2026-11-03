package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    private final FolderMapper folderMapper;
    private final FileTypeMapper fileTypeMapper;

    public FileMapper(FolderMapper folderMapper, FileTypeMapper fileTypeMapper) {
        this.folderMapper = folderMapper;
        this.fileTypeMapper = fileTypeMapper;
    }

    public File toDomain(FileEntity entity) {
        if (entity == null) {
            return null;
        }

        Folder folder = folderMapper.toDomain(entity.getFolder());
        FileType fileType = fileTypeMapper.toDomain(entity.getFileType());

        return File.reCreate(
                entity.getFileId(),
                folder,
                fileType,
                new FileName(entity.getName()),
                entity.getSize(),
                entity.getTotalPartitions(),
                entity.getDateCreated()
        );
    }

    public FileEntity toEntity(File domain, FolderEntity folderEntity, FileTypeEntity fileTypeEntity) {
        if (domain == null) {
            return null;
        }
        return new FileEntity(
                domain.getId(),
                folderEntity,
                fileTypeEntity,
                domain.getName() != null ? domain.getName().getValue() : null,
                domain.getBytesSize(),
                domain.getDateCreated(),
                domain.getLastTimeModified() != null ? domain.getLastTimeModified() : domain.getDateCreated(),
                domain.getTotalPartitions()
        );
    }
}

