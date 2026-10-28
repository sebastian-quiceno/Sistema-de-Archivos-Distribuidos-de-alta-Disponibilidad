package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.Folder;

import java.util.Optional;
import java.util.UUID;

public interface IFolderRepository {
    Optional<Folder> getById(UUID id);

    void save(Folder folder);

    void deleteById(UUID id);

}
