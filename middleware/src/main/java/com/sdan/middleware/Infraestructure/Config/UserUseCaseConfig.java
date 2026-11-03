package com.sdan.middleware.Infraestructure.Config;

import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Application.Users.CreateNewUserUseCase;
import com.sdan.middleware.Application.Users.UserLoginUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserUseCaseConfig {

    @Bean
    public CreateNewUserUseCase createNewUserUseCase(
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            ITokenGenerator tokenGenerator) {
        return new CreateNewUserUseCase(userRepository, passwordHasher, tokenGenerator);
    }

    @Bean
    public UserLoginUseCase userLoginUseCase(
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            ITokenGenerator tokenGenerator) {
        return new UserLoginUseCase(userRepository, passwordHasher, tokenGenerator);
    }
}

