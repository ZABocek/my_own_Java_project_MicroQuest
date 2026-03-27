package com.example.microquest.dto;

import jakarta.validation.constraints.*;

/**
 * Form DTO for new-user registration.
 * <p>
 * Validates username format (letters, digits, underscore, hyphen only),
 * email format, and enforces a strong-password policy via a regex pattern:
 * at least 8 characters with one uppercase, one lowercase, one digit, and
 * one special character.  The {@code confirmPassword} field is checked
 * against {@code password} in {@link com.example.microquest.service.AuthService}.
 * </p>
 */
public class RegistrationForm {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 40, message = "Username must be 3–40 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username may only contain letters, numbers, underscores and hyphens")
    private String username;

    @NotBlank(message = "Display name is required")
    @Size(max = 80, message = "Display name may not exceed 80 characters")
    private String displayName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 254)
    private String email;

    /**
     * Password rules: ≥8 chars, at least one uppercase, one lowercase, one digit, one special character.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @Size(max = 120)
    private String homeCity;

    @Size(max = 600)
    private String bio;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
