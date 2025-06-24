package com.plutus.mvp.service;

import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import com.plutus.mvp.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class RoleManagementService {
    private static final Logger logger = LoggerFactory.getLogger(RoleManagementService.class);

    @Inject
    private UserRepository userRepository;

    @Transactional
    public boolean assignRole(Long userId, UserRole role) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            
            if (userOptional.isEmpty()) {
                logger.warn("Attempt to assign role to non-existent user: {}", userId);
                return false;
            }

            User user = userOptional.get();
            
            // Check if role already exists to prevent duplicates
            if (!user.hasRole(role)) {
                user.addRole(role);
                logger.info("Role {} assigned to user {}", role, userId);
                return true;
            }

            logger.info("Role {} already exists for user {}", role, userId);
            return false;
        } catch (Exception e) {
            logger.error("Error assigning role to user", e);
            return false;
        }
    }

    @Transactional
    public boolean removeRole(Long userId, UserRole role) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            
            if (userOptional.isEmpty()) {
                logger.warn("Attempt to remove role from non-existent user: {}", userId);
                return false;
            }

            User user = userOptional.get();
            
            // Prevent removing the last role
            if (user.getRoles().size() > 1 || !role.equals(UserRole.USER)) {
                user.removeRole(role);
                logger.info("Role {} removed from user {}", role, userId);
                return true;
            }

            logger.warn("Cannot remove last role (USER) from user {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("Error removing role from user", e);
            return false;
        }
    }

    public boolean checkUserRole(Long userId, UserRole role) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        return userOptional.map(user -> user.hasRole(role))
            .orElse(false);
    }

    public Set<UserRole> getUserRoles(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        return userOptional.map(User::getRoles)
            .orElse(Set.of());
    }
}