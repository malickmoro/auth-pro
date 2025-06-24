package com.plutus.mvp.service;

import com.plutus.mvp.dto.UserRegistrationDTO;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserVerification;
import com.plutus.mvp.repository.UserRepository;
import com.plutus.mvp.repository.UserVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserVerificationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserVerificationRepository verificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserSendsVerificationCode() {
        // Arrange
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("StrongPassword123!");

        User savedUser = new User(dto.getFullName(), dto.getEmail(), "hashedPassword");
        savedUser.setId(1L);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.countRecentRegistrationsByIP(any(), anyInt())).thenReturn(0L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        User registeredUser = userService.registerUser(dto, "127.0.0.1");

        // Assert
        assertNotNull(registeredUser);
        verify(verificationRepository).save(any(UserVerification.class));
        verify(emailService).sendVerificationEmail(eq(dto.getEmail()), anyString());
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        Long userId = 1L;
        String verificationCode = "ABCDEF";
        
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        user.setId(userId);
        user.setEmailVerified(false);

        UserVerification verification = new UserVerification(userId, verificationCode, 15);

        when(verificationRepository.findByUserIdAndCode(userId, verificationCode))
            .thenReturn(Optional.of(verification));
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = userService.verifyEmail(userId, verificationCode);

        // Assert
        assertTrue(result);
        verify(verificationRepository).markVerified(verification);
        assertTrue(user.isEmailVerified());
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        // Arrange
        Long userId = 1L;
        String verificationCode = "ABCDEF";

        when(verificationRepository.findByUserIdAndCode(userId, verificationCode))
            .thenReturn(Optional.empty());

        // Act
        boolean result = userService.verifyEmail(userId, verificationCode);

        // Assert
        assertFalse(result);
    }
}