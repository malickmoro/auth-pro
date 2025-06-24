package com.plutus.mvp.repository;

import com.plutus.mvp.entity.RefreshToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public RefreshToken save(RefreshToken token) {
        em.persist(token);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        try {
            RefreshToken refreshToken = em.createQuery(
                "SELECT t FROM RefreshToken t " +
                "WHERE t.token = :token " +
                "AND t.used = false " +
                "AND t.expiresAt > CURRENT_TIMESTAMP", 
                RefreshToken.class
            )
            .setParameter("token", token)
            .getSingleResult();
            
            return Optional.of(refreshToken);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteExpiredTokens() {
        em.createQuery(
            "DELETE FROM RefreshToken t " +
            "WHERE t.expiresAt < CURRENT_TIMESTAMP OR t.used = true"
        ).executeUpdate();
    }

    @Transactional
    public void markTokenAsUsed(RefreshToken token) {
        token.setUsed(true);
        em.merge(token);
    }

    @Transactional
    public void deleteUserTokens(Long userId) {
        em.createQuery(
            "DELETE FROM RefreshToken t " +
            "WHERE t.userId = :userId"
        )
        .setParameter("userId", userId)
        .executeUpdate();
    }
}