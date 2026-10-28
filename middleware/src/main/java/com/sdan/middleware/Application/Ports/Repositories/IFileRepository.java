package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.File;

import java.util.Optional;
import java.util.UUID;

public interface IFileRepository {
    Optional<File> getById(UUID id);

    void save(File file);

    void deleteById(UUID id);
}
