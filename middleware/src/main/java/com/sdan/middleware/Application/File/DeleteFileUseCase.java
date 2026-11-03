package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Storage.IFileDeletionService;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;

import java.util.UUID;

public class DeleteFileUseCase {

    private final ICurrentUser currentUser;
    private final IFileRepository fileRepository;
    private final IFileDeletionService fileDeletionService;

    public DeleteFileUseCase(ICurrentUser currentUser, IFileRepository fileRepository, IFileDeletionService fileDeletionService) {
        this.currentUser = currentUser;
        this.fileRepository = fileRepository;
        this.fileDeletionService = fileDeletionService;
    }

    public void execute(UUID fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("El id del archivo no puede ser nulo");
        }

        UUID loggedUserId = currentUser.getUserId();

        File file = fileRepository.getById(fileId).orElseThrow(
                () -> new FileNotFoundException("No se encontro el archivo con el id: '" + fileId + "'")
        );

        if (!file.belongsToUser(loggedUserId)) {
            throw new FileDoesNotBelongToUserException(
                    "El archivo con el id: '" + fileId + "' no pertenece al usuario con el id: '" + loggedUserId + "'"
            );
        }

        fileDeletionService.deleteFile(file);
    }
}