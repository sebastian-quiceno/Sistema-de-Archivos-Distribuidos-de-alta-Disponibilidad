package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Security.Permissions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private ICurrentUser currentUser;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(currentUser);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAuthorize_hasPermission_succeeds() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority(Permissions.Files.READ))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(authorizationService.hasPermission(Permissions.Files.READ));
        assertDoesNotThrow(() -> authorizationService.authorize(Permissions.Files.READ));
    }

    @Test
    void testAuthorize_missingPermission_throwsAccessDeniedException() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority(Permissions.Files.READ))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(authorizationService.hasPermission(Permissions.Files.DELETE));
        assertThrows(AccessDeniedException.class, () -> authorizationService.authorize(Permissions.Files.DELETE));
    }

    @Test
    void testAuthorize_adminUser_hasAllPermissions() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(authorizationService.hasPermission(Permissions.Files.DELETE));
        assertDoesNotThrow(() -> authorizationService.authorize(Permissions.Files.DELETE));
    }

    @Test
    void testAuthorizeOwner_sameUser_succeeds() {
        UUID userId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(userId);

        assertDoesNotThrow(() -> authorizationService.authorizeOwner(userId));
    }

    @Test
    void testAuthorizeOwner_differentUserNonAdmin_throwsAccessDeniedException() {
        UUID loggedInUser = UUID.randomUUID();
        UUID ownerUser = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedInUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () -> authorizationService.authorizeOwner(ownerUser));
    }

    @Test
    void testAuthorizeOwner_differentUserAdmin_succeeds() {
        UUID loggedInUser = UUID.randomUUID();
        UUID ownerUser = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedInUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> authorizationService.authorizeOwner(ownerUser));
    }
}

