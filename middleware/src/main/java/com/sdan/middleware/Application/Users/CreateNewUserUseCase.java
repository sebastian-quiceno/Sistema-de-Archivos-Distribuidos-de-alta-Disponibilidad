package com.sdan.middleware.Application.Users;

import com.sdan.middleware.Application.Inputs.Commands.SignUpUserCommand;
import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;

public class CreateNewUserUseCase {
    private final IUserRepository userRepository;
    private final IFolderRepository folderRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenGenerator tokenGenerator;

    public CreateNewUserUseCase(IUserRepository userRepository, IFolderRepository folderRepository, IPasswordHasher passwordHasher, ITokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    public UserCredentialsOutput execute(SignUpUserCommand dto){

        if(userRepository.existByUsername(dto.userName().getValue()))
            throw new UsernameAlreadyExist("El nombre de usuario '"+dto.userName().getValue()+"' ya existe");

        PasswordHash passwordHashed = new PasswordHash(passwordHasher.Hash(dto.password()));

        System.out.println("Contraseña hasheada: "+ passwordHashed);

        User newuser = User.create(dto.userName(), passwordHashed);

        Folder rootFolder = Folder.create(newuser, null, new FolderName("root"));

        userRepository.save(newuser);

        folderRepository.save(rootFolder);

        String token = tokenGenerator.Generate(newuser);

        return new UserCredentialsOutput(newuser.getId(), newuser.getUsername().getValue(), token, rootFolder.getFolderId());
    }
}
