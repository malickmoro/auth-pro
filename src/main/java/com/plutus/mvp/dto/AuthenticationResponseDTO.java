package com.plutus.mvp.dto;

import java.util.List;

public class AuthenticationResponseDTO {
    private Long userId;
    private String email;
    private String accessToken;
    private String refreshToken;
    private List<String> roles;

    public AuthenticationResponseDTO() {}

    public AuthenticationResponseDTO(
        Long userId, 
        String email, 
        String accessToken, 
        String refreshToken,
        List<String> roles
    ) {
        this.userId = userId;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.roles = roles;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}