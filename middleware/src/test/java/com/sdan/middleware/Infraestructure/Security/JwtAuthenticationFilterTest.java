package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Application.Ports.Security.Permissions;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private ITokenGenerator tokenGenerator;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenGenerator, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilter_validBearerToken_authenticatesUser() throws ServletException, IOException {
        String token = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        User user = User.reCreate(userId, new UserName("alice"), new PasswordHash("hashedPass"), new Date());

        when(tokenGenerator.validateToken(token)).thenReturn(true);
        when(tokenGenerator.extractUserId(token)).thenReturn(userId);
        when(tokenGenerator.extractPermissions(token)).thenReturn(List.of(Permissions.Files.READ));
        when(userRepository.getById(userId)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getPrincipal() instanceof SecurityUserDetails);
        assertEquals(userId, ((SecurityUserDetails) auth.getPrincipal()).getUserId());
    }

    @Test
    void testDoFilter_invalidToken_doesNotAuthenticate() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(tokenGenerator.validateToken(token)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilter_userNotFoundInDb_doesNotAuthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        UUID userId = UUID.randomUUID();

        when(tokenGenerator.validateToken(token)).thenReturn(true);
        when(tokenGenerator.extractUserId(token)).thenReturn(userId);
        when(userRepository.getById(userId)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilter_noAuthorizationHeader_passesThroughWithoutAuth() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenGenerator);
    }
}

