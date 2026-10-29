package com.sdan.middleware.Application.Users;

import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Users.PasswordNotMatchException;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;


public class UserLoginUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenGenerator tokenGenerator;

    public UserLoginUseCase(IUserRepository userRepository, IPasswordHasher passwordHasher, ITokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    public UserCredentialsOutput execute(UserName userName, String password){

        User dbUser = userRepository.getByUsername(userName.getValue()).
                orElseThrow(
                        ()-> new UsernameNotFoundException("El nombre de usuario '"+userName+"' no existe")
                );
        PasswordHash passwordHash = new PasswordHash(passwordHasher.Hash(password));

        if(passwordHash.equals(dbUser.getPasswordHash()))
            throw new PasswordNotMatchException("Contraseña incorrecta");

        String token = tokenGenerator.Generate(dbUser);

        return new UserCredentialsOutput(dbUser.getId(), dbUser.getUsername(), token);


    }

}
