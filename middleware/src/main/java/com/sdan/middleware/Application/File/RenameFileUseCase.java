package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.RenameFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileName;

import java.util.UUID;

public class RenameFileUseCase {

    private final IFileRepository fileRepository;
    private final ICurrentUser currentUser;

    public RenameFileUseCase(IFileRepository fileRepository, ICurrentUser currentUser) {
        this.fileRepository = fileRepository;
        this.currentUser = currentUser;
    }

    public void execute(RenameFileCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.fileId() == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }
        if (command.newFileName() == null || command.newFileName().isBlank()) {
            throw new IllegalArgumentException("El nuevo nombre del archivo no puede estar vacío");
        }

        UUID loggedUserId = currentUser.getUserId();
        File file = fileRepository.getById(command.fileId()).orElseThrow(
                () -> new FileNotFoundException("No se encontró el archivo con el id: '" + command.fileId() + "'")
        );

        if (!file.belongsToUser(loggedUserId)) {
            throw new FileDoesNotBelongToUserException(
                    "El archivo con el id: '" + command.fileId() + "' no pertenece al usuario con el id: '" + loggedUserId + "'"
            );
        }

        file.changeName(new FileName(command.newFileName().strip()));
        fileRepository.save(file);
    }
}

