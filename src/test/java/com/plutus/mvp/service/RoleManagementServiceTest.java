package com.plutus.mvp.service;

import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import com.plutus.mvp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleManagementServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleManagementService roleManagementService;

    private User setupUser() {
        User user = new User("John Doe", "john@example.com", "password");
        user.setId(1L);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAssignRole_NewRole() {
        // Arrange
        User user = setupUser();
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = roleManagementService.assignRole(user.getId(), UserRole.ADMIN);

        // Assert
        assertTrue(result);
        assertTrue(user.hasRole(UserRole.ADMIN));
        assertTrue(user.hasRole(UserRole.USER)); // Original role remains
    }

    @Test
    void testAssignRole_DuplicateRole() {
        // Arrange
        User user = setupUser();
        user.addRole(UserRole.ADMIN);
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = roleManagementService.assignRole(user.getId(), UserRole.ADMIN);

        // Assert
        assertFalse(result);
    }

    @Test
    void testRemoveRole_ExistingRole() {
        // Arrange
        User user = setupUser();
        user.addRole(UserRole.ADMIN);
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = roleManagementService.removeRole(user.getId(), UserRole.ADMIN);

        // Assert
        assertTrue(result);
        assertFalse(user.hasRole(UserRole.ADMIN));
        assertTrue(user.hasRole(UserRole.USER)); // Cannot remove last role
    }

    @Test
    void testRemoveRole_LastRole() {
        // Arrange
        User user = setupUser();
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        boolean result = roleManagementService.removeRole(user.getId(), UserRole.USER);

        // Assert
        assertFalse(result);
        assertTrue(user.hasRole(UserRole.USER)); // Cannot remove last role
    }

    @Test
    void testCheckUserRole() {
        // Arrange
        User user = setupUser();
        user.addRole(UserRole.ADMIN);
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act & Assert
        assertTrue(roleManagementService.checkUserRole(user.getId(), UserRole.USER));
        assertTrue(roleManagementService.checkUserRole(user.getId(), UserRole.ADMIN));
        assertFalse(roleManagementService.checkUserRole(user.getId(), UserRole.MODERATOR));
    }

    @Test
    void testGetUserRoles() {
        // Arrange
        User user = setupUser();
        user.addRole(UserRole.ADMIN);
        user.addRole(UserRole.MODERATOR);
        
        when(userRepository.findById(user.getId()))
            .thenReturn(Optional.of(user));

        // Act
        Set<UserRole> roles = roleManagementService.getUserRoles(user.getId());

        // Assert
        assertEquals(3, roles.size()); // USER, ADMIN, MODERATOR
        assertTrue(roles.contains(UserRole.USER));
        assertTrue(roles.contains(UserRole.ADMIN));
        assertTrue(roles.contains(UserRole.MODERATOR));
    }
}