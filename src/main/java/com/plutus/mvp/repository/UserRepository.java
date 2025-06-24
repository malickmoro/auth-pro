package com.plutus.mvp.repository;

import com.plutus.mvp.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                .setParameter("email", email.trim())
                .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            Long count = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = LOWER(:email)", 
                Long.class
            )
            .setParameter("email", email.trim())
            .getSingleResult();
            
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void updateLastLoginTime(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        em.merge(user);
    }

    // Additional method to prevent rapid registrations
    public long countRecentRegistrationsByIP(String ipAddress, int minutesThreshold) {
        try {
            return em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.registrationIP = :ipAddress " +
                "AND u.registrationTime > CURRENT_TIMESTAMP - :threshold", 
                Long.class
            )
            .setParameter("ipAddress", ipAddress)
            .setParameter("threshold", minutesThreshold)
            .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }
}