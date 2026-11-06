package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.MetadataFileOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;

import java.util.UUID;

public class GetMetadataFileUseCase {
    private final ICurrentUser currentUser;
    private final IFileRepository fileRepository;

    public GetMetadataFileUseCase(ICurrentUser currentUser, IFileRepository fileRepository) {
        this.currentUser = currentUser;
        this.fileRepository = fileRepository;
    }

    public MetadataFileOutput execute(UUID fileId) {
        UUID loggedUserId = currentUser.getUserId();

        File file = fileRepository.getById(fileId).orElseThrow(
                () -> new FileNotFoundException("No se encontro el archivo con el id: '" + fileId + "'")
        );

        if (!file.belongsToUser(loggedUserId)) {
            throw new FileDoesNotBelongToUserException("El archivo con el id: '" + fileId + "' no pertenece al usuario con el id: '" + loggedUserId + "'");
        }

        return new MetadataFileOutput(
                file.getId(),
                file.getFolder().getFolderId(),
                file.getFileType().getName().getValue(),
                file.getName().getValue(),
                file.getBytesSize(),
                file.getTotalPartitions(),
                file.getLastTimeModified()
        );
    }
}
