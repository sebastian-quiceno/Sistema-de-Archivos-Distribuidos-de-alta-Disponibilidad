package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Outputs.FolderOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;

import java.util.List;
import java.util.UUID;

public class GetFoldersByFatherFolderUseCase {
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public GetFoldersByFatherFolderUseCase(IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public List<FolderOutput> execute(UUID folderId){
        UUID loggedUser = currentUser.getUserId();

        Folder folder = folderRepository.getById(folderId);

        if(folder == null)
            throw new FolderNotFoundException("No se encontro la carpeta con el id: '"+folderId+"'");

        if(!folder.getUser().getId().equals(loggedUser))
            throw new FolderDoesNotBelongToUserException(
                    "El usuario con el id: '"+loggedUser+"' no tiene acceso a la carpeta con el id: '"+folderId+"'"
            );

        List<Folder> relatedFolders = folderRepository.getRelatedFolders(folderId);

        return relatedFolders.stream().map(
                (f)-> new FolderOutput(
                        f.getFolderId(), f.getName().getValue(), f.getCreationDate()
                )).toList();
    }
}
