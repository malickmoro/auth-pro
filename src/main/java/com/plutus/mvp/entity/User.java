package com.plutus.mvp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @Column(name = "registration_ip")
    private String registrationIP;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    // Constructors
    public User() {
        this.registrationTime = LocalDateTime.now();
        this.emailVerified = false;
        // Default to USER role
        this.roles.add(UserRole.USER);
    }

    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.registrationTime = LocalDateTime.now();
        this.emailVerified = false;
        // Default to USER role
        this.roles.add(UserRole.USER);
    }

    // Existing getters and setters...

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    // Utility method to check if user has a specific role
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    // Convenience method to add a role
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    // Convenience method to remove a role
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }
}