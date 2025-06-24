package com.plutus.mvp.service;

import com.plutus.mvp.dto.AuditLogDTO;
import com.plutus.mvp.entity.AuditLogEntry;
import com.plutus.mvp.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogLoginAttempt() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        String ipAddress = "127.0.0.1";

        // Act
        auditService.logLoginAttempt(userId, username, ipAddress, true);

        // Assert
        verify(auditLogRepository).save(any(AuditLogEntry.class));
    }

    @Test
    void testGetPaginatedLogs() {
        // Arrange
        List<AuditLogEntry> mockLogs = Arrays.asList(
            createMockLogEntry(1L, "user1", AuditLogEntry.AuditActionType.LOGIN),
            createMockLogEntry(2L, "user2", AuditLogEntry.AuditActionType.PASSWORD_RESET_REQUEST)
        );

        when(auditLogRepository.findPaginatedLogs(1, 10))
            .thenReturn(mockLogs);
        when(auditLogRepository.countTotalLogs())
            .thenReturn(2L);

        // Act
        AuditLogDTO result = auditService.getPaginatedLogs(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getLogs().size());
        assertEquals(1, result.getPagination().getCurrentPage());
        assertEquals(10, result.getPagination().getPageSize());
        assertEquals(2, result.getPagination().getTotalItems());
    }

    @Test
    void testCleanupOldLogs() {
        // Act
        auditService.cleanupOldLogs(30);

        // Assert
        verify(auditLogRepository).deleteOldLogs(any(LocalDateTime.class));
    }

    // Helper method to create mock log entries
    private AuditLogEntry createMockLogEntry(
        Long userId, 
        String username, 
        AuditLogEntry.AuditActionType actionType
    ) {
        return new AuditLogEntry(
            userId,
            username,
            actionType,
            "127.0.0.1",
            "SUCCESS",
            null
        );
    }
}