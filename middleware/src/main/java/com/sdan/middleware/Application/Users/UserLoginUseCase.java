package com.sdan.middleware.Application.Users;

import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Users.PasswordNotMatchException;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.UserName;

public class UserLoginUseCase {
    private final IUserRepository userRepository;
    private final IFolderRepository folderRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenGenerator tokenGenerator;

    public UserLoginUseCase(
            IUserRepository userRepository,
            IFolderRepository folderRepository,
            IPasswordHasher passwordHasher,
            ITokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    public UserCredentialsOutput execute(UserName userName, String password){

        User dbUser = userRepository.getByUsername(userName.getValue()).
                orElseThrow(
                        ()-> new UsernameNotFoundException("El nombre de usuario '"+userName+"' no existe")
                );

        if(!passwordHasher.Verify(password, dbUser.getPasswordHash().getValue()))
            throw new PasswordNotMatchException("Contraseña incorrecta");

        String token = tokenGenerator.Generate(dbUser);

        Folder rootFolder = folderRepository.getRootFolderByUserId(dbUser.getId())
                .orElseGet(() -> {
                    Folder newRoot = Folder.create(dbUser, null, new FolderName("root"));
                    folderRepository.save(newRoot);
                    return newRoot;
                });

        return new UserCredentialsOutput(
                dbUser.getId(),
                dbUser.getUsername().getValue(),
                token,
                rootFolder.getFolderId()
        );
    }

}
