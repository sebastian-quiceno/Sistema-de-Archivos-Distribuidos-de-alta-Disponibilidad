package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.FileDownloadOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Storage.IFileReconstructionService;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;

import java.io.InputStream;
import java.util.UUID;

public class GetFileUseCase {

    private final ICurrentUser currentUser;
    private final IFileRepository fileRepository;
    private final IFileReconstructionService fileReconstructionService;

    public GetFileUseCase(ICurrentUser currentUser, IFileRepository fileRepository, IFileReconstructionService fileReconstructionService) {
        this.currentUser = currentUser;
        this.fileRepository = fileRepository;
        this.fileReconstructionService = fileReconstructionService;
    }

    public FileDownloadOutput execute(UUID fileId) {
        UUID loggedUserId = currentUser.getUserId();

        File file = fileRepository.getById(fileId).orElseThrow(
                () -> new FileNotFoundException("No se encontro el archivo con el id: '" + fileId + "'")
        );

        if (!file.belongsToUser(loggedUserId)) {
            throw new FileDoesNotBelongToUserException("El archivo con el id: '" + fileId + "' no pertenece al usuario con el id: '" + loggedUserId + "'");
        }

        InputStream stream = fileReconstructionService.mergeFile(file);
        return new FileDownloadOutput(stream, file.getName().getValue());
    }
}
