package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuthenticationResponseDTO;
import com.plutus.mvp.dto.LoginDTO;
import com.plutus.mvp.dto.UserRegistrationDTO;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import com.plutus.mvp.entity.UserVerification;
import com.plutus.mvp.repository.UserRepository;
import com.plutus.mvp.repository.UserVerificationRepository;
import com.plutus.mvp.security.JWTUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_VALIDITY_MINUTES = 15;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserVerificationRepository verificationRepository;

    @Inject
    private JWTUtil jwtUtil;

    @Inject
    private AuditService auditService;
    
    private EmailService emailService;

    @Transactional
    public AuthenticationResponseDTO login(@Valid LoginDTO loginDTO, String ipAddress) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(loginDTO.getEmail());

            if (userOptional.isEmpty()) {
                logger.warn("Login attempt with non-existent email: {} from IP {}",
                        loginDTO.getEmail(), ipAddress);

                // Log failed login attempt
                auditService.logLoginAttempt(null, loginDTO.getEmail(), ipAddress, false);

                return null;
            }

            User user = userOptional.get();

            // Check if email is verified (optional, can be configured)
            if (!user.isEmailVerified()) {
                logger.warn("Login attempt for unverified email: {} from IP {}",
                        loginDTO.getEmail(), ipAddress);

                // Log failed login attempt
                auditService.logLoginAttempt(user.getId(), user.getEmail(), ipAddress, false);

                return null;
            }

            // Verify password (using BCrypt for secure comparison)
            boolean passwordMatches = BCrypt.checkpw(loginDTO.getPassword(), user.getPassword());

            if (passwordMatches) {
                // Update last login time
                userRepository.updateLastLoginTime(user);

                // Generate JWT token
                String token = jwtUtil.generateAccessToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);

                // Log successful login
                auditService.logLoginAttempt(user.getId(), user.getEmail(), ipAddress, true);

                // Log user roles for potential access control
                Set<UserRole> userRoles = user.getRoles();
                logger.info("Successful login for user: {} from IP {} with roles: {}",
                        loginDTO.getEmail(), ipAddress, userRoles);

                // Create authentication response
                return new AuthenticationResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        token,
                        refreshToken,
                        userRoles.stream()
                                .map(Enum::name)
                                .collect(Collectors.toList())
                );
            } else {
                logger.warn("Failed login attempt for email: {} from IP {}",
                        loginDTO.getEmail(), ipAddress);

                // Log failed login attempt
                auditService.logLoginAttempt(user.getId(), user.getEmail(), ipAddress, false);

                return null;
            }
        } catch (Exception e) {
            logger.error("Error during login process for email: {} from IP {}",
                    loginDTO.getEmail(), ipAddress, e);
            return null;
        }
    }

    @Transactional
    public User registerUser(@Valid UserRegistrationDTO registrationDTO, String ipAddress) {
        try {
            // Validate email uniqueness (case-insensitive)
            if (userRepository.existsByEmail(registrationDTO.getEmail())) {
                logger.warn("Registration attempt with existing email: {}", registrationDTO.getEmail());
                throw new IllegalArgumentException("Email already in use");
            }

            // Check for rapid registrations from same IP
            long recentRegistrations = userRepository.countRecentRegistrationsByIP(ipAddress, 5);
            if (recentRegistrations >= 3) {
                logger.warn("Too many registration attempts from IP: {}", ipAddress);
                throw new IllegalStateException("Too many registration attempts. Please try again later.");
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(registrationDTO.getPassword(), BCrypt.gensalt());

            // Create new user
            User newUser = new User(
                    registrationDTO.getFullName(),
                    registrationDTO.getEmail(),
                    hashedPassword
            );

            // Set registration IP
            newUser.setRegistrationIP(ipAddress);

            // Save the user
            User savedUser = userRepository.save(newUser);

            // Generate and save verification code
            String verificationCode = generateVerificationCode();
            UserVerification verification = new UserVerification(
                    savedUser.getId(),
                    verificationCode,
                    VERIFICATION_VALIDITY_MINUTES
            );
            verificationRepository.save(verification);

            // Send verification email
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationCode);

            logger.info("User registered successfully: {} ({}) from IP {}",
                    savedUser.getFullName(), savedUser.getEmail(), ipAddress);

            return savedUser;
        } catch (Exception e) {
            logger.error("Error during user registration from IP {}", ipAddress, e);
            throw e;
        }
    }

    @Transactional
    public boolean verifyEmail(Long userId, String verificationCode) {
        try {
            // Find a valid, non-expired verification
            Optional<UserVerification> optVerification = verificationRepository
                    .findByUserIdAndCode(userId, verificationCode);

            if (optVerification.isEmpty()) {
                logger.warn("Invalid or expired verification code for user {}", userId);
                return false;
            }

            // Mark verification as complete
            UserVerification verification = optVerification.get();
            verificationRepository.markVerified(verification);

            // Update user's email verification status
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            user.setEmailVerified(true);

            logger.info("Email verified successfully for user {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Error verifying email for user {}", userId, e);
            return false;
        }
    }

    // Generate a secure random verification code
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[VERIFICATION_CODE_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, VERIFICATION_CODE_LENGTH)
                .toUpperCase();
    }
}
