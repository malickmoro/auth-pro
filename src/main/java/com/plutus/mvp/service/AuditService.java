package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuditLogDTO;
import com.plutus.mvp.entity.AuditLogEntry;
import com.plutus.mvp.repository.AuditLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Inject
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logEvent(AuditLogEntry logEntry) {
        try {
            auditLogRepository.save(logEntry);
            logger.info("Audit log created: {} for user {}", 
                logEntry.getActionType(), 
                logEntry.getUsername()
            );
        } catch (Exception e) {
            logger.error("Error creating audit log", e);
        }
    }

    @Transactional
    public void logLoginAttempt(
        Long userId, 
        String username, 
        String ipAddress, 
        boolean success
    ) {
        AuditLogEntry logEntry = new AuditLogEntry(
            userId,
            username,
            AuditLogEntry.AuditActionType.LOGIN,
            ipAddress,
            success ? "SUCCESS" : "FAILED",
            null
        );
        
        logEvent(logEntry);
    }

    @Transactional
    public void logPasswordResetRequest(
        Long userId, 
        String username, 
        String ipAddress, 
        boolean success
    ) {
        AuditLogEntry logEntry = new AuditLogEntry(
            userId,
            username,
            AuditLogEntry.AuditActionType.PASSWORD_RESET_REQUEST,
            ipAddress,
            success ? "SUCCESS" : "FAILED",
            null
        );
        
        logEvent(logEntry);
    }

    @Transactional
    public void logTokenRefresh(
        Long userId, 
        String username, 
        String ipAddress, 
        boolean success
    ) {
        AuditLogEntry logEntry = new AuditLogEntry(
            userId,
            username,
            AuditLogEntry.AuditActionType.TOKEN_REFRESH,
            ipAddress,
            success ? "SUCCESS" : "FAILED",
            null
        );
        
        logEvent(logEntry);
    }

    @Transactional
    public AuditLogDTO getPaginatedLogs(int page, int pageSize) {
        // Fetch paginated logs
        List<AuditLogEntry> logs = auditLogRepository.findPaginatedLogs(page, pageSize);
        
        // Count total logs
        long totalLogs = auditLogRepository.countTotalLogs();
        
        // Convert to DTOs
        List<AuditLogDTO.LogEntryDTO> logEntries = logs.stream()
            .map(log -> new AuditLogDTO.LogEntryDTO(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getActionType().name(),
                log.getIpAddress(),
                log.getOutcome(),
                log.getTimestamp(),
                log.getAdditionalDetails()
            ))
            .collect(Collectors.toList());

        // Create pagination metadata
        AuditLogDTO.PaginationMetadata metadata = new AuditLogDTO.PaginationMetadata(
            page,
            pageSize,
            totalLogs,
            (int) Math.ceil((double) totalLogs / pageSize)
        );

        return new AuditLogDTO(logEntries, metadata);
    }

    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteOldLogs(cutoffDate);
        
        logger.info("Cleaned up audit logs older than {}", cutoffDate);
    }
}