package com.plutus.mvp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Full name can only contain letters, spaces, hyphens, and apostrophes")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email must be less than 255 characters")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 64, message = "Password must be between 12 and 64 characters")
    @Pattern.List({
        @Pattern(regexp = "(?=.*[A-Z])", message = "Password must contain at least one uppercase letter"),
        @Pattern(regexp = "(?=.*[a-z])", message = "Password must contain at least one lowercase letter"),
        @Pattern(regexp = "(?=.*\\d)", message = "Password must contain at least one number"),
        @Pattern(regexp = "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])", message = "Password must contain at least one special character")
    })
    private String password;

    // No-args constructor
    public UserRegistrationDTO() {}

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}