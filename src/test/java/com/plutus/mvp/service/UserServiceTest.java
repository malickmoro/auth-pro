package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuthenticationResponseDTO;
import com.plutus.mvp.dto.LoginDTO;
import com.plutus.mvp.entity.User;
import com.plutus.mvp.repository.UserRepository;
import com.plutus.mvp.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User setupUser() {
        User user = new User("John Doe", "john@example.com", 
            BCrypt.hashpw("password123", BCrypt.gensalt()));
        user.setId(1L);
        user.setEmailVerified(true);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Successful() {
        // Arrange
        User user = setupUser();
        LoginDTO loginDTO = new LoginDTO("john@example.com", "password123");
        String ipAddress = "127.0.0.1";

        // Prepare mocks for JWT generation
        when(userRepository.findByEmail(loginDTO.getEmail()))
            .thenReturn(Optional.of(user));
        
        // Mock JWT token generation with three parameters
        when(jwtUtil.generateAccessToken(eq(user)))
            .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(eq(user)))
            .thenReturn("refresh-token");

        // Act
        AuthenticationResponseDTO response = userService.login(loginDTO, ipAddress);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        
        // Verify interactions
        verify(userRepository).updateLastLoginTime(user);
        verify(auditService).logLoginAttempt(
            eq(user.getId()), 
            eq(user.getEmail()), 
            eq(ipAddress), 
            eq(true)
        );
    }

    @Test
    void testLogin_InvalidEmail() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("nonexistent@example.com", "password123");
        String ipAddress = "127.0.0.1";

        when(userRepository.findByEmail(loginDTO.getEmail()))
            .thenReturn(Optional.empty());

        // Act
        AuthenticationResponseDTO response = userService.login(loginDTO, ipAddress);

        // Assert
        assertNull(response);
        verify(auditService).logLoginAttempt(
            eq(null), 
            eq(loginDTO.getEmail()), 
            eq(ipAddress), 
            eq(false)
        );
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        User user = setupUser();
        LoginDTO loginDTO = new LoginDTO("john@example.com", "wrongpassword");
        String ipAddress = "127.0.0.1";

        when(userRepository.findByEmail(loginDTO.getEmail()))
            .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponseDTO response = userService.login(loginDTO, ipAddress);

        // Assert
        assertNull(response);
        verify(auditService).logLoginAttempt(
            eq(user.getId()), 
            eq(user.getEmail()), 
            eq(ipAddress), 
            eq(false)
        );
    }

    @Test
    void testLogin_UnverifiedEmail() {
        // Arrange
        User user = setupUser();
        user.setEmailVerified(false);
        LoginDTO loginDTO = new LoginDTO("john@example.com", "password123");
        String ipAddress = "127.0.0.1";

        when(userRepository.findByEmail(loginDTO.getEmail()))
            .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponseDTO response = userService.login(loginDTO, ipAddress);

        // Assert
        assertNull(response);
        verify(auditService).logLoginAttempt(
            eq(user.getId()), 
            eq(user.getEmail()), 
            eq(ipAddress), 
            eq(false)
        );
    }
}