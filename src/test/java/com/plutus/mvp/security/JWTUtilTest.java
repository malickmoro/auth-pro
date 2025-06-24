package com.plutus.mvp.security;

import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {
    private JWTUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        
        testUser = new User("Test User", "test@example.com", "password");
        testUser.setId(1L);
        testUser.addRole(UserRole.USER);
        testUser.addRole(UserRole.ADMIN);
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtUtil.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken() {
        // Arrange
        String token = jwtUtil.generateAccessToken(testUser);

        // Act & Assert
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testParseToken() {
        // Arrange
        String token = jwtUtil.generateAccessToken(testUser);

        // Act
        Claims claims = jwtUtil.parseToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getEmail(), claims.get("email"));
    }

    @Test
    void testGetUserIdFromToken() {
        // Arrange
        String token = jwtUtil.generateAccessToken(testUser);

        // Act
        Long userId = jwtUtil.getUserIdFromToken(token);

        // Assert
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void testGetRolesFromToken() {
        // Arrange
        String token = jwtUtil.generateAccessToken(testUser);

        // Act
        var roles = jwtUtil.getRolesFromToken(token);

        // Assert
        assertEquals(2, roles.size());
        assertTrue(roles.contains(UserRole.USER));
        assertTrue(roles.contains(UserRole.ADMIN));
    }

    @Test
    void testInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertFalse(jwtUtil.validateToken(invalidToken));
        assertThrows(RuntimeException.class, () -> jwtUtil.parseToken(invalidToken));
    }
}