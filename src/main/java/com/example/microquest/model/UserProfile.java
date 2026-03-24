package com.example.microquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_user_profile_username", columnList = "username"),
        @Index(name = "idx_user_profile_display_name", columnList = "displayName"),
        @Index(name = "idx_user_profile_email", columnList = "email")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(unique = true, length = 254)
    private String email;

    @Column(length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ROLE_USER;

    /** False only for permanently banned accounts. */
    @Column(nullable = false)
    private boolean active = true;

    /** Non-null while a temporary ban is active. */
    @Column
    private LocalDateTime bannedUntil;

    /** Counts completed bans for escalation: 0=1-month next, 1=3-month next, 2+=permanent. */
    @Column(nullable = false)
    private int banCount = 0;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(length = 100)
    private String emailVerificationToken;

    /** Path on disk to the user's uploaded photo ID. Only visible to admins. */
    @Column(length = 500)
    private String photoIdPath;

    @Column(length = 120)
    private String homeCity;

    @Column(length = 600)
    private String bio;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Quest> authoredQuests = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<QuestSave> savedQuests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestSubmission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BanRecord> banRecords = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /** Returns true if a temporary ban is currently active. */
    public boolean isCurrentlyBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }

    // â”€â”€ Getters & Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }

    public int getBanCount() { return banCount; }
    public void setBanCount(int banCount) { this.banCount = banCount; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String t) { this.emailVerificationToken = t; }

    public String getPhotoIdPath() { return photoIdPath; }
    public void setPhotoIdPath(String photoIdPath) { this.photoIdPath = photoIdPath; }

    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Quest> getAuthoredQuests() { return authoredQuests; }
    public void setAuthoredQuests(List<Quest> authoredQuests) { this.authoredQuests = authoredQuests; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<QuestSave> getSavedQuests() { return savedQuests; }
    public void setSavedQuests(List<QuestSave> savedQuests) { this.savedQuests = savedQuests; }

    public List<QuestSubmission> getSubmissions() { return submissions; }
    public void setSubmissions(List<QuestSubmission> submissions) { this.submissions = submissions; }

    public List<BanRecord> getBanRecords() { return banRecords; }
    public void setBanRecords(List<BanRecord> banRecords) { this.banRecords = banRecords; }
}
