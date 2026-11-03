package com.sdan.middleware.Application.Users;

import com.sdan.middleware.Application.Inputs.Commands.SignUpUserCommand;
import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateNewUserUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private ITokenGenerator tokenGenerator;

    private CreateNewUserUseCase createNewUserUseCase;

    @BeforeEach
    void setUp() {
        createNewUserUseCase = new CreateNewUserUseCase(userRepository, passwordHasher, tokenGenerator);
    }

    @Test
    void testExecute_newUser_createsAndReturnsCredentials() {
        SignUpUserCommand command = new SignUpUserCommand(new UserName("newuser"), "securePass123");

        when(userRepository.existByUsername("newuser")).thenReturn(false);
        when(passwordHasher.Hash("securePass123")).thenReturn("hashedPass123");
        when(tokenGenerator.Generate(any(User.class))).thenReturn("mockJwtToken");

        UserCredentialsOutput output = createNewUserUseCase.execute(command);

        assertNotNull(output);
        assertEquals("newuser", output.userName().getValue());
        assertEquals("mockJwtToken", output.token());
        assertNotNull(output.userId());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testExecute_existingUsername_throwsUsernameAlreadyExist() {
        SignUpUserCommand command = new SignUpUserCommand(new UserName("existinguser"), "pass123");

        when(userRepository.existByUsername("existinguser")).thenReturn(true);

        assertThrows(UsernameAlreadyExist.class, () -> createNewUserUseCase.execute(command));
        verify(userRepository, never()).save(any(User.class));
    }
}