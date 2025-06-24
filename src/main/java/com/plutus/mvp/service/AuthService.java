package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuthenticationResponseDTO;
import com.plutus.mvp.dto.RefreshTokenRequestDTO;
import com.plutus.mvp.entity.RefreshToken;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.repository.RefreshTokenRepository;
import com.plutus.mvp.repository.UserRepository;
import com.plutus.mvp.security.JWTUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private RefreshTokenRepository refreshTokenRepository;

    @Inject
    private JWTUtil jwtUtil;

    @Transactional
    public AuthenticationResponseDTO refreshToken(@Valid RefreshTokenRequestDTO refreshTokenRequest) {
        try {
            // Find and validate refresh token
            Optional<RefreshToken> tokenOptional = refreshTokenRepository
                .findByToken(refreshTokenRequest.getRefreshToken());

            if (tokenOptional.isEmpty()) {
                logger.warn("Invalid or expired refresh token used");
                return null;
            }

            RefreshToken refreshToken = tokenOptional.get();

            // Find user
            Optional<User> userOptional = userRepository.findById(refreshToken.getUserId());

            if (userOptional.isEmpty()) {
                logger.error("User not found for refresh token");
                return null;
            }

            User user = userOptional.get();

            // Generate new tokens
            String newAccessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            // Mark old refresh token as used
            refreshTokenRepository.markTokenAsUsed(refreshToken);

            // Create and save new refresh token
            RefreshToken newRefreshTokenEntity = new RefreshToken(
                user.getId(), 
                7 // 7 days expiration
            );
            refreshTokenRepository.save(newRefreshTokenEntity);

            // Log the token refresh
            logger.info("Token refreshed for user: {}", user.getEmail());

            // Return new authentication response
            return new AuthenticationResponseDTO(
                user.getId(), 
                user.getEmail(), 
                newAccessToken, 
                newRefreshTokenEntity.getToken(),
                user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            return null;
        }
    }

    @Transactional
    public void invalidateUserTokens(Long userId) {
        try {
            // Delete all refresh tokens for the user
            refreshTokenRepository.deleteUserTokens(userId);
            logger.info("All refresh tokens invalidated for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error invalidating user tokens", e);
        }
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        try {
            Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(refreshToken);
            
            if (tokenOptional.isEmpty()) {
                logger.warn("Invalid refresh token");
                return null;
            }

            return tokenOptional.get().getUserId();
        } catch (Exception e) {
            logger.error("Error extracting user ID from refresh token", e);
            return null;
        }
    }
}