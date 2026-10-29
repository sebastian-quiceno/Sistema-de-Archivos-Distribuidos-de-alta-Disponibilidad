package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;

import java.util.UUID;

public class DeleteFolderUseCase {
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public DeleteFolderUseCase(IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public void execute(UUID folderId){
        UUID loggedUser = currentUser.getUserId();

        Folder folder = folderRepository.getById(folderId);

        if(folder == null)
            throw new FolderNotFoundException("No se encontro la carpeta con el id: '"+folderId+"'");

        if(!folder.getUser().getId().equals(loggedUser))
            throw new FolderDoesNotBelongToUserException(
                    "La carpeta con el id: '"+folderId+"' no se puede borrar porque no pertenece al usuario con el id: '"+loggedUser+"'"
            );

        folderRepository.deleteById(folderId);
    }
}
