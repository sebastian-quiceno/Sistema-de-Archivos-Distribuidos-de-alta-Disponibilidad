package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class FileTypeMapper {

    public FileType toDomain(FileTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return FileType.reCreate(
                entity.getFileTypeId(),
                new FileTypeName(entity.getName()),
                entity.getDescription()
        );
    }

    public FileTypeEntity toEntity(FileType domain) {
        if (domain == null) {
            return null;
        }
        return new FileTypeEntity(
                domain.getId(),
                domain.getName() != null ? domain.getName().getValue() : null,
                domain.getDescription()
        );
    }
}

