package com.sdan.middleware.Infraestructure.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Sha256FileHasherTest {

    private Sha256FileHasher fileHasher;

    @BeforeEach
    void setUp() {
        fileHasher = new Sha256FileHasher();
    }

    @Test
    void testHash_returns64CharLowercaseHex() {
        String content = "Distributed Fault-Tolerant Storage Block Content";
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String hash = fileHasher.Hash(stream);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[a-f0-9]{64}$"));
    }

    @Test
    void testVerify_matchingContent_returnsTrue() {
        String content = "Chunk test payload";
        ByteArrayInputStream stream1 = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String hash = fileHasher.Hash(stream1);

        ByteArrayInputStream stream2 = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        assertTrue(fileHasher.Verify(stream2, hash));
    }

    @Test
    void testVerify_corruptedContent_returnsFalse() {
        String originalContent = "Original valid block data";
        ByteArrayInputStream stream1 = new ByteArrayInputStream(originalContent.getBytes(StandardCharsets.UTF_8));
        String hash = fileHasher.Hash(stream1);

        String corruptedContent = "Corrupted block data!";
        ByteArrayInputStream stream2 = new ByteArrayInputStream(corruptedContent.getBytes(StandardCharsets.UTF_8));
        assertFalse(fileHasher.Verify(stream2, hash));
    }

    @Test
    void testVerify_caseInsensitiveMatch() {
        String content = "Hello DFSha";
        ByteArrayInputStream stream1 = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String hash = fileHasher.Hash(stream1);

        ByteArrayInputStream stream2 = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        assertTrue(fileHasher.Verify(stream2, hash.toUpperCase()));
    }

    @Test
    void testHash_nullStream_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> fileHasher.Hash(null));
    }

    @Test
    void testVerify_nullOrBlankInputs_returnsFalse() {
        ByteArrayInputStream stream = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        assertFalse(fileHasher.Verify(null, "someHash"));
        assertFalse(fileHasher.Verify(stream, null));
        assertFalse(fileHasher.Verify(stream, "   "));
    }
}

