package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Inputs.Commands.CreateFolderCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IAuthorizationService;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Users.UserNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FolderName;

import java.util.UUID;

//TODO: REVISAR como obtener el usuario para validar si puede crearlo o no
public class CreateFolderUseCase {
    private final IUserRepository userRepository;
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public CreateFolderUseCase(IUserRepository userRepository, IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public void execute(CreateFolderCommand dto){

        UUID loggedUser = currentUser.getUserId();

        User user = userRepository.getById(loggedUser).orElseThrow(
                () -> new UserNotFoundException("No se encontro el usuario con el id :'"+loggedUser+"'")
        );

        //Nullable
        Folder fatherFolder = folderRepository.getById(dto.fatherId());

        if(fatherFolder != null && !fatherFolder.getUser().getId().equals(loggedUser))
            throw new FolderDoesNotBelongToUserException(
                    "La carpeta con el id: '"+fatherFolder.getFolder_id()+"' no pertenece al usuario con el id:'"+loggedUser
            );

        FolderName validFolderName = new FolderName(dto.folderName());

        Folder newFolder = Folder.create(user, fatherFolder, validFolderName);

        folderRepository.save(newFolder);

    }
}
