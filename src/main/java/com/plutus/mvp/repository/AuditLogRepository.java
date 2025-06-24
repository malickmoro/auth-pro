package com.plutus.mvp.repository;

import com.plutus.mvp.entity.AuditLogEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AuditLogRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(AuditLogEntry logEntry) {
        em.persist(logEntry);
    }

    public List<AuditLogEntry> findPaginatedLogs(int page, int pageSize) {
        TypedQuery<AuditLogEntry> query = em.createQuery(
            "SELECT a FROM AuditLogEntry a ORDER BY a.timestamp DESC", 
            AuditLogEntry.class
        );
        
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        
        return query.getResultList();
    }

    public long countTotalLogs() {
        return em.createQuery(
            "SELECT COUNT(a) FROM AuditLogEntry a", 
            Long.class
        ).getSingleResult();
    }

    @Transactional
    public void deleteOldLogs(LocalDateTime cutoffDate) {
        em.createQuery(
            "DELETE FROM AuditLogEntry a WHERE a.timestamp < :cutoffDate"
        )
        .setParameter("cutoffDate", cutoffDate)
        .executeUpdate();
    }
}