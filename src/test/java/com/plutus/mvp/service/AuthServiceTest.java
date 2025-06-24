package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuthenticationResponseDTO;
import com.plutus.mvp.dto.RefreshTokenRequestDTO;
import com.plutus.mvp.entity.RefreshToken;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import com.plutus.mvp.repository.RefreshTokenRepository;
import com.plutus.mvp.repository.UserRepository;
import com.plutus.mvp.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User setupUser() {
        User user = new User("John Doe", "john@example.com", "password");
        user.setId(1L);
        user.addRole(UserRole.USER);
        return user;
    }

    private RefreshToken setupRefreshToken(User user) {
        RefreshToken token = new RefreshToken(user.getId(), 7);
        return token;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRefreshToken_Successful() {
        // Arrange
        User user = setupUser();
        RefreshToken oldRefreshToken = setupRefreshToken(user);
        RefreshTokenRequestDTO refreshTokenRequest = new RefreshTokenRequestDTO(oldRefreshToken.getToken());

        // Mock behaviors
        when(refreshTokenRepository.findByToken(oldRefreshToken.getToken()))
            .thenReturn(Optional.of(oldRefreshToken));
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user))
            .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(user))
            .thenReturn("new-refresh-token");

        // Act
        AuthenticationResponseDTO response = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        // Verify interactions
        verify(refreshTokenRepository).markTokenAsUsed(oldRefreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testRefreshToken_ExpiredToken() {
        // Arrange
        User user = setupUser();
        RefreshToken expiredToken = setupRefreshToken(user);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired token
        RefreshTokenRequestDTO refreshTokenRequest = new RefreshTokenRequestDTO(expiredToken.getToken());

        // Mock behaviors
        when(refreshTokenRepository.findByToken(expiredToken.getToken()))
            .thenReturn(Optional.empty());

        // Act
        AuthenticationResponseDTO response = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNull(response);

        // Verify interactions
        verify(refreshTokenRepository, never()).markTokenAsUsed(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void testRefreshToken_UsedToken() {
        // Arrange
        User user = setupUser();
        RefreshToken usedToken = setupRefreshToken(user);
        usedToken.setUsed(true);
        RefreshTokenRequestDTO refreshTokenRequest = new RefreshTokenRequestDTO(usedToken.getToken());

        // Mock behaviors
        when(refreshTokenRepository.findByToken(usedToken.getToken()))
            .thenReturn(Optional.empty());

        // Act
        AuthenticationResponseDTO response = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNull(response);

        // Verify interactions
        verify(refreshTokenRepository, never()).markTokenAsUsed(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void testInvalidateUserTokens() {
        // Arrange
        Long userId = 1L;

        // Act
        authService.invalidateUserTokens(userId);

        // Assert
        verify(refreshTokenRepository).deleteUserTokens(userId);
    }
}