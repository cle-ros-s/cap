package com.govscheme.service;

import com.govscheme.dto.AuthResponse;
import com.govscheme.dto.RegisterRequest;
import com.govscheme.entity.User;
import com.govscheme.entity.UserProfile;
import com.govscheme.repository.SavedSchemeRepository;
import com.govscheme.repository.UserProfileRepository;
import com.govscheme.repository.UserRepository;
import com.govscheme.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("User Registration Password Hashing Tests")
class UserServicePasswordEncryptionTest {

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private SavedSchemeRepository savedSchemeRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        savedSchemeRepository = mock(SavedSchemeRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "userProfileRepository", userProfileRepository);
        ReflectionTestUtils.setField(userService, "savedSchemeRepository", savedSchemeRepository);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

        // Standalone fake token provider without bytecode inlining
        JwtTokenProvider tokenProvider = new JwtTokenProvider() {
            @Override
            public String generateToken(Authentication authentication) {
                return "test-jwt-token";
            }
        };
        ReflectionTestUtils.setField(userService, "tokenProvider", tokenProvider);

        // Standalone fake authentication manager
        AuthenticationManager authManager = authentication -> authentication;
        ReflectionTestUtils.setField(userService, "authenticationManager", authManager);
    }

    @Test
    @DisplayName("Sign-up must encode password with BCrypt before saving in database")
    void testSignUpEncodesPasswordWithBCrypt() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ramesh Kumar");
        request.setEmail("ramesh@gov.in");
        request.setMobile("9876501234");
        request.setAadhaarNumber("123456789012");
        request.setAadhaarVerified(true);
        request.setPassword("SecretPlainPassword123!");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByMobile(anyString())).thenReturn(false);
        when(userRepository.existsByAadhaarNumber(anyString())).thenReturn(false);

        // When saved, return user with ID
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.builder()
                    .id(100L)
                    .name(u.getName())
                    .email(u.getEmail())
                    .mobile(u.getMobile())
                    .aadhaarNumber(u.getAadhaarNumber())
                    .aadhaarVerified(u.getAadhaarVerified())
                    .password(u.getPassword())
                    .role(u.getRole())
                    .status(u.getStatus())
                    .build();
        });

        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AuthResponse response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("ramesh@gov.in", response.getEmail());

        // Capture saved user to inspect password
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());

        User savedUser = userCaptor.getAllValues().get(0);
        assertNotNull(savedUser.getPassword());

        // 1. Password must NEVER be saved as plain text
        assertNotEquals("SecretPlainPassword123!", savedUser.getPassword(),
                "CRITICAL: User password must not be stored as plain text in the database");

        // 2. Password must start with standard BCrypt hash signature
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$"),
                "User password must be a valid BCrypt hash");

        // 3. BCrypt hash must have the expected standard length of 60 characters
        assertEquals(60, savedUser.getPassword().length(),
                "BCrypt hash length must be 60 characters");

        // 4. BCryptPasswordEncoder must successfully verify the original raw password against saved hash
        assertTrue(passwordEncoder.matches("SecretPlainPassword123!", savedUser.getPassword()),
                "BCryptPasswordEncoder must verify original password with stored salt-hash");

        // 5. BCryptPasswordEncoder must fail when verified against an invalid password
        assertFalse(passwordEncoder.matches("WrongPassword!2026", savedUser.getPassword()),
                "BCryptPasswordEncoder must reject non-matching passwords");
    }
}
