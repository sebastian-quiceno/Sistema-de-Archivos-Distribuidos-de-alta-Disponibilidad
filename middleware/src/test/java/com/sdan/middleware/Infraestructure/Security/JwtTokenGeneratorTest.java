package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.Permissions;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenGeneratorTest {

    private static final String SECRET = "thisIsASecretKeyWithMoreThan256BitsForHmacSha256Security1234567890!";
    private static final long TTL = 3600000L; // 1 hour

    private JwtTokenGenerator tokenGenerator;
    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tokenGenerator = new JwtTokenGenerator(SECRET, TTL);
        userId = UUID.randomUUID();
        testUser = User.reCreate(userId, new UserName("testuser"), new PasswordHash("hashedpass"), new Date());
    }

    @Test
    void testGenerateAndValidate_validToken_returnsTrue() {
        String token = tokenGenerator.Generate(testUser);

        assertNotNull(token);
        assertTrue(tokenGenerator.validateToken(token));
    }

    @Test
    void testExtractUserId_returnsCorrectUUID() {
        String token = tokenGenerator.Generate(testUser);

        UUID extractedId = tokenGenerator.extractUserId(token);
        assertEquals(userId, extractedId);
    }

    @Test
    void testExtractUsername_returnsCorrectUsername() {
        String token = tokenGenerator.Generate(testUser);

        String extractedUsername = tokenGenerator.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testExtractPermissions_containsDefaultPermissions() {
        String token = tokenGenerator.Generate(testUser);

        List<String> perms = tokenGenerator.extractPermissions(token);
        assertNotNull(perms);
        assertTrue(perms.contains(Permissions.Files.READ));
        assertTrue(perms.contains(Permissions.Files.CREATE));
        assertTrue(perms.contains(Permissions.Folders.READ));
    }

    @Test
    void testValidateToken_expiredToken_returnsFalse() {
        // Create generator with negative TTL so token is already expired
        JwtTokenGenerator expiredGenerator = new JwtTokenGenerator(SECRET, -1000L);
        String expiredToken = expiredGenerator.Generate(testUser);

        assertFalse(tokenGenerator.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_tamperedToken_returnsFalse() {
        String token = tokenGenerator.Generate(testUser);
        // Tamper signature by replacing characters in signature part
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(tokenGenerator.validateToken(tamperedToken));
    }

    @Test
    void testValidateToken_differentKey_returnsFalse() {
        String anotherSecret = "anotherDifferentSecretKeyWithMoreThan256BitsForSecurity1234567890!";
        JwtTokenGenerator anotherGenerator = new JwtTokenGenerator(anotherSecret, TTL);

        String tokenFromAnotherKey = anotherGenerator.Generate(testUser);
        assertFalse(tokenGenerator.validateToken(tokenFromAnotherKey));
    }

    @Test
    void testValidateToken_nullOrMalformed_returnsFalse() {
        assertFalse(tokenGenerator.validateToken(null));
        assertFalse(tokenGenerator.validateToken(""));
        assertFalse(tokenGenerator.validateToken("invalid.jwt.token"));
    }

    @Test
    void testGenerate_withNullUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenGenerator.Generate(null));
    }

    @Test
    void testConstructor_shortSecret_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenGenerator("shortKey", TTL));
    }
}

