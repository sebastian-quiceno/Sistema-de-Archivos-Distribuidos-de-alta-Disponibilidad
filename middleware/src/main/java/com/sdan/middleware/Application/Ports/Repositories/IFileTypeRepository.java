package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.FileType;

import java.util.Optional;
import java.util.UUID;

public interface IFileTypeRepository {

    Optional<FileType> getById(UUID id);

    Optional<FileType> getByName(String name);

    void save(FileType fileType);

    void deleteById(UUID id);
}
