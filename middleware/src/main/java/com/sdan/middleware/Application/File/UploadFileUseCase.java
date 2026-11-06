package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.*;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileName;

import java.util.UUID;

public class UploadFileUseCase {

    private final IFileTypeRepository fileTypeRepository;
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;
    private final FileService fileService;

    public UploadFileUseCase(IFileTypeRepository fileTypeRepository, IFolderRepository folderRepository, ICurrentUser currentUser, FileService fileService) {
        this.fileTypeRepository = fileTypeRepository;
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
        this.fileService = fileService;
    }

    public void execute(CreateFileCommand dto){
        fileService.validateCommand(dto);

        UUID loggedUser = currentUser.getUserId();

        Folder folder = folderRepository.getById(dto.folderId());

        if(folder == null)
            throw new FolderNotFoundException("La carpeta con el id: '"+dto.folderId()+" no existe");

        if(!(folder.getUser().getId().equals(loggedUser)))
            throw new FolderDoesNotBelongToUserException(
                    "No se puede subir el archivo porque la carpeta con el id: '"+dto.folderId()+"' no pertenece al usuario con el id: '"+loggedUser+"'"
            );

        FileType fileType = fileTypeRepository.getByName(dto.fileTypeName()).orElseThrow(
                ()-> new FileTypeNotFoundException("No se encontro el FileType con el id: '"+dto.fileTypeName()+"'")
        );

        FileName fileName = new FileName(dto.name());

        fileService.uploadFile(fileName, fileType, folder, dto.file(), dto.size());
    }
}
