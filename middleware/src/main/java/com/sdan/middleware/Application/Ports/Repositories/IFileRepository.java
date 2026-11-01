package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFileRepository {
    Optional<File> getById(UUID id);

    List<Chunk> getOriginalsChunks(UUID fileId);

    void save(File file);

    void deleteById(UUID id);
}
