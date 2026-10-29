package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Inputs.Commands.MoveFolderCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;

import java.util.UUID;


//TODO: configurar validacion por medio de
public class MoveFolderUserCase {
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public MoveFolderUserCase(IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public void execute(MoveFolderCommand dto) {
        UUID loggedUser = currentUser.getUserId();

        Folder dbFolder = folderRepository.getById(dto.folderId());

        if (dbFolder == null) {
            throw new FolderNotFoundException("No se encontró la carpeta con el id: '" + dto.folderId() + "'");
        }

        if(!(loggedUser.equals(dbFolder.getUser().getId())))
            throw new FolderDoesNotBelongToUserException("La carpeta con id: '"+dto.folderId()+"' no se puede mover porque no pertenece al usuario con id: '"+loggedUser+"'");

        Folder fatherFolder = null;

        if (dto.newFatherFolderId() != null) {
            fatherFolder = folderRepository.getById(dto.newFatherFolderId());

            if (fatherFolder == null) {
                throw new FolderNotFoundException("No se encontró la carpeta padre con el id: '"+ dto.newFatherFolderId() + "'");
            }
        }

        dbFolder.changeFatherFolder(fatherFolder);

        folderRepository.save(dbFolder);
    }
}
