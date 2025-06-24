package com.plutus.mvp.service;

import com.plutus.mvp.dto.PasswordResetConfirmDTO;
import com.plutus.mvp.dto.PasswordResetRequestDTO;
import com.plutus.mvp.entity.PasswordResetToken;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.repository.PasswordResetTokenRepository;
import com.plutus.mvp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User setupUser() {
        User user = new User("John Doe", "john@example.com", 
            BCrypt.hashpw("oldpassword", BCrypt.gensalt()));
        user.setId(1L);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestPasswordReset_ExistingEmail() {
        // Arrange
        User user = setupUser();
        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO("john@example.com");

        when(userRepository.findByEmail(requestDTO.getEmail()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = passwordResetService.requestPasswordReset(requestDTO);

        // Assert
        assertTrue(result);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), anyString());
    }

    @Test
    void testRequestPasswordReset_NonExistentEmail() {
        // Arrange
        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO("nonexistent@example.com");

        when(userRepository.findByEmail(requestDTO.getEmail()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = passwordResetService.requestPasswordReset(requestDTO);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void testConfirmPasswordReset_ValidToken() {
        // Arrange
        User user = setupUser();
        PasswordResetToken token = new PasswordResetToken(user.getId(), 30);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        PasswordResetConfirmDTO resetDTO = new PasswordResetConfirmDTO(
            token.getToken(), 
            "newStrongPassword123!"
        );

        when(tokenRepository.findByToken(token.getToken()))
            .thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = passwordResetService.confirmPasswordReset(resetDTO);

        // Assert
        assertTrue(result);
        verify(tokenRepository).markTokenAsUsed(token);
        
        // Verify password was updated (not comparing exact hash)
        assertNotEquals(user.getPassword(), BCrypt.hashpw("oldpassword", BCrypt.gensalt()));
    }

    @Test
    void testConfirmPasswordReset_InvalidToken() {
        // Arrange
        PasswordResetConfirmDTO resetDTO = new PasswordResetConfirmDTO(
            "invalidToken", 
            "newStrongPassword123!"
        );

        when(tokenRepository.findByToken(resetDTO.getToken()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = passwordResetService.confirmPasswordReset(resetDTO);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).markTokenAsUsed(any());
    }

    @Test
    void testConfirmPasswordReset_ExpiredToken() {
        // Arrange
        User user = setupUser();
        PasswordResetToken token = new PasswordResetToken(user.getId(), 30);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(31)); // Expired token

        PasswordResetConfirmDTO resetDTO = new PasswordResetConfirmDTO(
            token.getToken(), 
            "newStrongPassword123!"
        );

        when(tokenRepository.findByToken(token.getToken()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = passwordResetService.confirmPasswordReset(resetDTO);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).markTokenAsUsed(any());
    }
}