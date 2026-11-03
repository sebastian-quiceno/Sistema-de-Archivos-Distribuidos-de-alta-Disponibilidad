package com.sdan.middleware.Infraestructure.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BcryptPasswordHasherTest {

    private BcryptPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new BcryptPasswordHasher();
    }

    @Test
    void testHash_generatesValidBcryptHash() {
        String password = "securePassword123!";
        String hash = passwordHasher.Hash(password);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertEquals(60, hash.length());
        assertNotEquals(password, hash);
    }

    @Test
    void testVerify_withCorrectPassword_returnsTrue() {
        String password = "mySecretPassword#99";
        String hash = passwordHasher.Hash(password);

        assertTrue(passwordHasher.Verify(password, hash));
    }

    @Test
    void testVerify_withIncorrectPassword_returnsFalse() {
        String password = "mySecretPassword#99";
        String hash = passwordHasher.Hash(password);

        assertFalse(passwordHasher.Verify("wrongPassword", hash));
    }

    @Test
    void testHash_differentSaltsForSamePassword() {
        String password = "samePassword";
        String hash1 = passwordHasher.Hash(password);
        String hash2 = passwordHasher.Hash(password);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordHasher.Verify(password, hash1));
        assertTrue(passwordHasher.Verify(password, hash2));
    }

    @Test
    void testHash_withNullOrBlank_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.Hash(null));
        assertThrows(IllegalArgumentException.class, () -> passwordHasher.Hash("   "));
    }

    @Test
    void testVerify_withNullInputs_returnsFalse() {
        assertFalse(passwordHasher.Verify(null, "someHash"));
        assertFalse(passwordHasher.Verify("password", null));
        assertFalse(passwordHasher.Verify(null, null));
    }
}

