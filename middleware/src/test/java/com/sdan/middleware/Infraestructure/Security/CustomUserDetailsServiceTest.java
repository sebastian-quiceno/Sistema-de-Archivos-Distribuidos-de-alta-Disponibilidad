package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.Permissions;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private IUserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_existingUser_returnsUserDetailsWithAuthorities() {
        UUID userId = UUID.randomUUID();
        User user = User.reCreate(userId, new UserName("alice"), new PasswordHash("hashedPassword123"), new Date());
        when(userRepository.getByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertNotNull(details);
        assertEquals("alice", details.getUsername());
        assertEquals("hashedPassword123", details.getPassword());
        assertTrue(details instanceof SecurityUserDetails);
        assertEquals(userId, ((SecurityUserDetails) details).getUserId());

        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Permissions.Files.READ)));
    }

    @Test
    void testLoadUserByUsername_nonExistentUser_throwsUsernameNotFoundException() {
        when(userRepository.getByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("bob"));
    }

    @Test
    void testLoadUserByUsername_nullOrBlank_throwsUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(null));
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("   "));
    }
}

