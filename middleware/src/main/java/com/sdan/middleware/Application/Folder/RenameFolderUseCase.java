package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Inputs.Commands.MoveFolderCommand;
import com.sdan.middleware.Application.Outputs.RenameFolderCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FolderName;

import java.util.UUID;

public class RenameFolderUseCase {
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public RenameFolderUseCase(IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public void execute(RenameFolderCommand dto) {
        UUID loggedUser = currentUser.getUserId();

        Folder dbFolder = folderRepository.getById(dto.folderId());

        if (dbFolder == null) {
            throw new FolderNotFoundException("No se encontró la carpeta con el id: '" + dto.folderId() + "'");
        }

        if(!(loggedUser.equals(dbFolder.getUser().getId())))
            throw new FolderDoesNotBelongToUserException("La carpeta con id: '"+dto.folderId()+"' no se puede mover porque no pertenece al usuario con id: '"+loggedUser+"'");

        dbFolder.changeFolderName(new FolderName(dto.newForlderName()));

        folderRepository.save(dbFolder);
    }
}
