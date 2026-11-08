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
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFolderRepository folderRepository;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private ITokenGenerator tokenGenerator;

    private UserLoginUseCase userLoginUseCase;
    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userLoginUseCase = new UserLoginUseCase(userRepository, folderRepository, passwordHasher, tokenGenerator);
        userId = UUID.randomUUID();
        testUser = User.reCreate(userId, new UserName("bob"), new PasswordHash("$2a$10$storedHash"), new Date());
    }

    @Test
    void testExecute_validCredentials_returnsTokenAndRootFolderId() {
        UUID rootFolderId = UUID.randomUUID();
        Folder rootFolder = Folder.reCreate(rootFolderId, testUser, null, new FolderName("root"), new Date());

        when(userRepository.getByUsername("bob")).thenReturn(Optional.of(testUser));
        when(passwordHasher.Verify("correctPassword", "$2a$10$storedHash")).thenReturn(true);
        when(tokenGenerator.Generate(testUser)).thenReturn("validTokenBob");
        when(folderRepository.getRootFolderByUserId(userId)).thenReturn(Optional.of(rootFolder));

        UserCredentialsOutput output = userLoginUseCase.execute(new UserName("bob"), "correctPassword");

        assertNotNull(output);
        assertEquals(userId, output.userId());
        assertEquals("bob", output.userName());
        assertEquals("validTokenBob", output.token());
        assertEquals(rootFolderId, output.rootFolderId());
    }

    @Test
    void testExecute_validCredentials_whenUserHasNoRootFolder_createsRootFolderAndReturnsCredentials() {
        when(userRepository.getByUsername("bob")).thenReturn(Optional.of(testUser));
        when(passwordHasher.Verify("correctPassword", "$2a$10$storedHash")).thenReturn(true);
        when(tokenGenerator.Generate(testUser)).thenReturn("validTokenBob");
        when(folderRepository.getRootFolderByUserId(userId)).thenReturn(Optional.empty());

        UserCredentialsOutput output = userLoginUseCase.execute(new UserName("bob"), "correctPassword");

        assertNotNull(output);
        assertEquals(userId, output.userId());
        assertEquals("bob", output.userName());
        assertEquals("validTokenBob", output.token());
        assertNotNull(output.rootFolderId());

        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    void testExecute_invalidPassword_throwsPasswordNotMatchException() {
        when(userRepository.getByUsername("bob")).thenReturn(Optional.of(testUser));
        when(passwordHasher.Verify("wrongPassword", "$2a$10$storedHash")).thenReturn(false);

        assertThrows(PasswordNotMatchException.class, () ->
                userLoginUseCase.execute(new UserName("bob"), "wrongPassword")
        );
        verify(tokenGenerator, never()).Generate(any());
        verify(folderRepository, never()).getRootFolderByUserId(any());
    }

    @Test
    void testExecute_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.getByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userLoginUseCase.execute(new UserName("nonexistent"), "anyPassword")
        );
        verify(folderRepository, never()).getRootFolderByUserId(any());
    }
}