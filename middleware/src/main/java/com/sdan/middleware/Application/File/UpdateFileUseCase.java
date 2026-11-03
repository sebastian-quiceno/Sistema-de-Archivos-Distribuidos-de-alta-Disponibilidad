package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.UpdateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Storage.IFileUpdateService;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;

import java.util.UUID;

public class UpdateFileUseCase {

    private final ICurrentUser currentUser;
    private final IFileRepository fileRepository;
    private final IFileUpdateService fileUpdateService;

    public UpdateFileUseCase(ICurrentUser currentUser, IFileRepository fileRepository, IFileUpdateService fileUpdateService) {
        this.currentUser = currentUser;
        this.fileRepository = fileRepository;
        this.fileUpdateService = fileUpdateService;
    }

    public void execute(UpdateFileCommand command) {
        validateCommand(command);

        UUID loggedUserId = currentUser.getUserId();

        File file = fileRepository.getById(command.fileId()).orElseThrow(
                () -> new FileNotFoundException("No se encontro el archivo con el id: '" + command.fileId() + "'")
        );

        if (!file.belongsToUser(loggedUserId)) {
            throw new FileDoesNotBelongToUserException(
                    "El archivo con el id: '" + command.fileId() + "' no pertenece al usuario con el id: '" + loggedUserId + "'"
            );
        }

        fileUpdateService.updateFile(file, command.newFile(), command.newSize());
    }

    //TODO: revisar si dejar la validacion en el caso de uso o sacarla a una nueva clase
    public void validateCommand(UpdateFileCommand command){
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }

        if (command.fileId() == null) {
            throw new IllegalArgumentException("El id del archivo no puede ser nulo");
        }

        if (command.newFile() == null) {
            throw new IllegalArgumentException("El contenido del archivo no puede ser nulo");
        }

        if (command.newSize() == null || command.newSize() < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser nulo ni negativo");
        }
    }
}

