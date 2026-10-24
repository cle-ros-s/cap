package com.govscheme.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Encryption & Security Tests")
class PasswordEncryptionTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("BCryptPasswordEncoder hashes passwords and does not store plain text")
    void testPasswordIsNotPlainText() {
        String plainPassword = "MySecurePassword@2026";
        String encodedPassword = passwordEncoder.encode(plainPassword);

        // 1. Password must never equal the raw plain text
        assertNotEquals(plainPassword, encodedPassword, "Password must not be stored as plain text");

        // 2. Encoded password must start with standard BCrypt signature ($2a$ or $2b$)
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
                "Encoded password must be a valid BCrypt hash format");

        // 3. BCrypt hash must be 60 characters long
        assertEquals(60, encodedPassword.length(), "BCrypt hash length must be 60 characters");

        // 4. Verification must match original password
        assertTrue(passwordEncoder.matches(plainPassword, encodedPassword),
                "BCryptPasswordEncoder must verify matching plain password against hash");

        // 5. Verification must reject incorrect password
        assertFalse(passwordEncoder.matches("WrongPassword@123", encodedPassword),
                "BCryptPasswordEncoder must reject incorrect password");
    }

    @Test
    @DisplayName("BCrypt uses cryptographic salt - identical plain passwords generate different hashes")
    void testBCryptSaltRandomization() {
        String plainPassword = "SamePassword123";
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);

        // Hashes should differ due to random 128-bit salt
        assertNotEquals(hash1, hash2, "BCrypt must salt each hash so identical passwords produce different ciphertexts");

        // Both distinct hashes must still correctly verify the original password
        assertTrue(passwordEncoder.matches(plainPassword, hash1));
        assertTrue(passwordEncoder.matches(plainPassword, hash2));
    }
}
