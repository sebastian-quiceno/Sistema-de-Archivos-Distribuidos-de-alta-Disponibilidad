package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Folder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFolderRepository {
    Folder getById(UUID id);

    List<Folder> getRelatedFolders(UUID id);

    List<Folder> getRelatedFoldersByUserId(UUID userId);

    Optional<Folder> getRootFolderByUserId(UUID userId);

    List<File> getRelatedFiles(UUID id);

    List<File> getRelatedFileByUserId(UUID userId);

    void save(Folder folder);

    void deleteById(UUID id);

}
