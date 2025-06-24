package com.plutus.mvp.repository;

import com.plutus.mvp.entity.UserVerification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserVerificationRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public UserVerification save(UserVerification verification) {
        em.persist(verification);
        return verification;
    }

    public Optional<UserVerification> findByUserIdAndCode(Long userId, String verificationCode) {
        try {
            UserVerification verification = em.createQuery(
                "SELECT v FROM UserVerification v " +
                "WHERE v.userId = :userId " +
                "AND v.verificationCode = :code " +
                "AND v.verified = false " +
                "AND v.expiresAt > CURRENT_TIMESTAMP", 
                UserVerification.class
            )
            .setParameter("userId", userId)
            .setParameter("code", verificationCode)
            .getSingleResult();
            
            return Optional.of(verification);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteExpiredVerifications() {
        em.createQuery(
            "DELETE FROM UserVerification v " +
            "WHERE v.expiresAt < CURRENT_TIMESTAMP"
        ).executeUpdate();
    }

    @Transactional
    public void markVerified(UserVerification verification) {
        verification.setVerified(true);
        em.merge(verification);
    }
}