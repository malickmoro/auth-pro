package com.plutus.mvp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AuditLogDTO {
    private List<LogEntryDTO> logs;
    private PaginationMetadata pagination;

    public AuditLogDTO() {}

    public AuditLogDTO(List<LogEntryDTO> logs, PaginationMetadata pagination) {
        this.logs = logs;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<LogEntryDTO> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntryDTO> logs) {
        this.logs = logs;
    }

    public PaginationMetadata getPagination() {
        return pagination;
    }

    public void setPagination(PaginationMetadata pagination) {
        this.pagination = pagination;
    }

    // Nested DTOs for log entry and pagination
    public static class LogEntryDTO {
        private Long id;
        private Long userId;
        private String username;
        private String actionType;
        private String ipAddress;
        private String outcome;
        private LocalDateTime timestamp;
        private String additionalDetails;

        public LogEntryDTO() {}

        public LogEntryDTO(
            Long id, 
            Long userId, 
            String username, 
            String actionType, 
            String ipAddress, 
            String outcome, 
            LocalDateTime timestamp, 
            String additionalDetails
        ) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.actionType = actionType;
            this.ipAddress = ipAddress;
            this.outcome = outcome;
            this.timestamp = timestamp;
            this.additionalDetails = additionalDetails;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getOutcome() {
            return outcome;
        }

        public void setOutcome(String outcome) {
            this.outcome = outcome;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getAdditionalDetails() {
            return additionalDetails;
        }

        public void setAdditionalDetails(String additionalDetails) {
            this.additionalDetails = additionalDetails;
        }
    }

    public static class PaginationMetadata {
        private int currentPage;
        private int pageSize;
        private long totalItems;
        private int totalPages;

        public PaginationMetadata() {}

        public PaginationMetadata(
            int currentPage, 
            int pageSize, 
            long totalItems, 
            int totalPages
        ) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalItems = totalItems;
            this.totalPages = totalPages;
        }

        // Getters and Setters
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}