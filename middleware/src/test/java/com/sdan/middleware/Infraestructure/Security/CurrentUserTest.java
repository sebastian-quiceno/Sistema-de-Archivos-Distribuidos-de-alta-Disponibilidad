package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Domain.Exceptions.Users.UnauthorizedUserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserTest {

    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new CurrentUser();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetUserId_authenticatedSecurityUserDetails_returnsUserId() {
        UUID expectedId = UUID.randomUUID();
        SecurityUserDetails userDetails = new SecurityUserDetails(
                expectedId,
                "john_doe",
                "hashedPass",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(expectedId, currentUser.getUserId());
        assertEquals("john_doe", currentUser.getUsername());
        assertTrue(currentUser.isAuthenticated());
    }

    @Test
    void testGetUserId_unauthenticated_throwsUnauthorizedUserException() {
        assertThrows(UnauthorizedUserException.class, () -> currentUser.getUserId());
        assertThrows(UnauthorizedUserException.class, () -> currentUser.getUsername());
        assertFalse(currentUser.isAuthenticated());
    }

    @Test
    void testGetUserId_anonymousUser_throwsUnauthorizedUserException() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThrows(UnauthorizedUserException.class, () -> currentUser.getUserId());
        assertThrows(UnauthorizedUserException.class, () -> currentUser.getUsername());
        assertFalse(currentUser.isAuthenticated());
    }
}

