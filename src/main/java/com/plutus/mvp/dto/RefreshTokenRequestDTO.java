package com.plutus.mvp.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshTokenRequestDTO() {}

    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}