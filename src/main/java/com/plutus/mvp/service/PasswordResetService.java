package com.plutus.mvp.service;

import com.plutus.mvp.dto.PasswordResetConfirmDTO;
import com.plutus.mvp.dto.PasswordResetRequestDTO;
import com.plutus.mvp.entity.PasswordResetToken;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.repository.PasswordResetTokenRepository;
import com.plutus.mvp.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRATION_MINUTES = 30;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordResetTokenRepository tokenRepository;

    @Inject
    private EmailService emailService;

    @Transactional
    public boolean requestPasswordReset(@Valid PasswordResetRequestDTO requestDTO) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(requestDTO.getEmail());

            if (userOptional.isEmpty()) {
                logger.warn("Password reset requested for non-existent email: {}", requestDTO.getEmail());
                return false;
            }

            User user = userOptional.get();

            // Generate and save reset token
            PasswordResetToken resetToken = new PasswordResetToken(user.getId(), TOKEN_EXPIRATION_MINUTES);
            tokenRepository.save(resetToken);

            // Send reset email (currently mocked)
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());

            logger.info("Password reset token generated for user: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error during password reset request", e);
            return false;
        }
    }

    @Transactional
    public boolean confirmPasswordReset(@Valid PasswordResetConfirmDTO resetDTO) {
        try {
            // Find and validate token
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(resetDTO.getToken());

            if (tokenOptional.isEmpty()) {
                logger.warn("Invalid or expired password reset token used");
                return false;
            }

            PasswordResetToken token = tokenOptional.get();

            // Find user
            Optional<User> userOptional = userRepository.findById(token.getUserId());

            if (userOptional.isEmpty()) {
                logger.error("User not found for password reset token");
                return false;
            }

            User user = userOptional.get();

            // Hash new password
            String hashedPassword = BCrypt.hashpw(resetDTO.getNewPassword(), BCrypt.gensalt());

            // Update user password
            user.setPassword(hashedPassword);

            // Mark token as used
            tokenRepository.markTokenAsUsed(token);

            logger.info("Password reset successful for user: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error during password reset confirmation", e);
            return false;
        }
    }

    // Clean up expired tokens (can be called periodically)
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }
}