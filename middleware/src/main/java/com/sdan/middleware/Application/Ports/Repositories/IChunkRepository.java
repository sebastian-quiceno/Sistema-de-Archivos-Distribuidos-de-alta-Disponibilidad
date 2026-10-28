package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.ValueObjects.FileHash;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IChunkRepository{
    Optional<Chunk> getById(UUID id);

    //Revisar si hace falta (Mejor quitar)
    List<Chunk> getAll();

    FileHash getFileHash(UUID id);

    void save(Chunk chunk);

    void deleteById(UUID id);

}
