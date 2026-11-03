package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    private UserMapper userMapper;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        userRepository = new UserRepository(springDataUserRepository, userMapper);
    }

    @Test
    void testGetById_found() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "testuser", "hashed", new Date());
        when(springDataUserRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<User> result = userRepository.getById(id);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername().getValue());
    }

    @Test
    void testGetById_null_returnsEmpty() {
        Optional<User> result = userRepository.getById(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(springDataUserRepository);
    }

    @Test
    void testGetByUsername_found() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "admin", "pass", new Date());
        when(springDataUserRepository.findByUsername("admin")).thenReturn(Optional.of(entity));

        Optional<User> result = userRepository.getByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testExistByUsername() {
        when(springDataUserRepository.existsByUsername("admin")).thenReturn(true);
        when(springDataUserRepository.existsByUsername("nonexistent")).thenReturn(false);

        assertTrue(userRepository.existByUsername("admin"));
        assertFalse(userRepository.existByUsername("nonexistent"));
        assertFalse(userRepository.existByUsername(null));
    }

    @Test
    void testSave() {
        User user = User.create(new UserName("newuser"), new PasswordHash("pwd123"));

        userRepository.save(user);

        verify(springDataUserRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        userRepository.deleteById(id);

        verify(springDataUserRepository, times(1)).deleteById(id);
    }
}

