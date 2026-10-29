package com.sdan.middleware.Application.Users;

import com.sdan.middleware.Application.Inputs.Commands.SignUpUserCommand;
import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;

public class CreateNewUserUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenGenerator tokenGenerator;

    public CreateNewUserUseCase(IUserRepository userRepository, IPasswordHasher passwordHasher, ITokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    public UserCredentialsOutput execute(SignUpUserCommand dto){

        if(userRepository.existByUsername(dto.userName().getValue()))
            throw new UsernameAlreadyExist("El nombre de usuario '"+dto.userName().getValue()+"' ya existe");

        PasswordHash passwordHashed = new PasswordHash(passwordHasher.Hash(dto.password()));

        User newuser = User.create(dto.userName(), passwordHashed);

        userRepository.save(newuser);

        String token = tokenGenerator.Generate(newuser);

        return new UserCredentialsOutput(newuser.getId(), newuser.getUsername(), token);
    }

}
