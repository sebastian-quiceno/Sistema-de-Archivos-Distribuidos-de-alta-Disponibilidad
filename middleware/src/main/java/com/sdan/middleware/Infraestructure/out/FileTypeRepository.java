package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IFileTypeRepository;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FileTypeMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class FileTypeRepository implements IFileTypeRepository {

    private final SpringDataFileTypeRepository springDataFileTypeRepository;
    private final FileTypeMapper fileTypeMapper;

    public FileTypeRepository(SpringDataFileTypeRepository springDataFileTypeRepository, FileTypeMapper fileTypeMapper) {
        this.springDataFileTypeRepository = springDataFileTypeRepository;
        this.fileTypeMapper = fileTypeMapper;
    }

    @Override
    public Optional<FileType> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataFileTypeRepository.findById(id).map(fileTypeMapper::toDomain);
    }

    @Override
    public Optional<FileType> getByName(String name) {
        FileTypeEntity type = springDataFileTypeRepository.findByName(name).orElseThrow(
                ()-> new FileTypeNotFoundException("No se encontro el tipo de archivo con el nombre: "+name)
        );

        return Optional.ofNullable(fileTypeMapper.toDomain(type));
    }

    @Override
    public void save(FileType fileType) {
        if (fileType == null) {
            return;
        }
        FileTypeEntity entity = fileTypeMapper.toEntity(fileType);
        springDataFileTypeRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataFileTypeRepository.deleteById(id);
    }
}

