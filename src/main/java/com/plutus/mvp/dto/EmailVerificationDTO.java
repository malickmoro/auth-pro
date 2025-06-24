package com.plutus.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class EmailVerificationDTO {
    @Positive(message = "User ID must be a positive number")
    private Long userId;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;

    // Constructors
    public EmailVerificationDTO() {}

    public EmailVerificationDTO(Long userId, String verificationCode) {
        this.userId = userId;
        this.verificationCode = verificationCode;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}