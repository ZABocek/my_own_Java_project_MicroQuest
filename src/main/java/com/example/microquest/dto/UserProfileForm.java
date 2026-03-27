package com.example.microquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form DTO for creating or editing a user profile (admin user-management utility).
 * Note: this path does not reset passwords; password management is separate
 * via {@link com.example.microquest.dto.PasswordChangeForm}.
 */
public class UserProfileForm {

    @NotBlank
    @Size(max = 40)
    private String username;

    @NotBlank
    @Size(max = 80)
    private String displayName;

    @Size(max = 120)
    private String homeCity;

    @Size(max = 600)
    private String bio;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(String homeCity) {
        this.homeCity = homeCity;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
