package com.plutus.mvp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AuditActionType actionType;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "outcome", nullable = false)
    private String outcome;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "additional_details")
    private String additionalDetails;

    // Enum for action types
    public enum AuditActionType {
        LOGIN,
        LOGOUT,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_CONFIRM,
        TOKEN_REFRESH,
        ROLE_CHANGE,
        USER_REGISTRATION,
        EMAIL_VERIFICATION
    }

    // Constructors
    public AuditLogEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLogEntry(Long userId, String username, AuditActionType actionType, 
                         String ipAddress, String outcome, String additionalDetails) {
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.ipAddress = ipAddress;
        this.outcome = outcome;
        this.timestamp = LocalDateTime.now();
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

    public AuditActionType getActionType() {
        return actionType;
    }

    public void setActionType(AuditActionType actionType) {
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