package com.plutus.mvp.repository;

import com.plutus.mvp.entity.PasswordResetToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class PasswordResetTokenRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        em.persist(token);
        return token;
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        try {
            PasswordResetToken resetToken = em.createQuery(
                "SELECT t FROM PasswordResetToken t " +
                "WHERE t.token = :token " +
                "AND t.used = false " +
                "AND t.expiresAt > CURRENT_TIMESTAMP", 
                PasswordResetToken.class
            )
            .setParameter("token", token)
            .getSingleResult();
            
            return Optional.of(resetToken);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteExpiredTokens() {
        em.createQuery(
            "DELETE FROM PasswordResetToken t " +
            "WHERE t.expiresAt < CURRENT_TIMESTAMP OR t.used = true"
        ).executeUpdate();
    }

    @Transactional
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        em.merge(token);
    }
}