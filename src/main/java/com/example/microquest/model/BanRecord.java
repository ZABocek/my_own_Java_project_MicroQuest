package com.example.microquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ban_records")
public class BanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    /** Duration tier of this ban. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BanTier tier;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime bannedAt;

    /** Null for permanent bans. */
    @Column
    private LocalDateTime expiresAt;

    /** Whether an appeal has been submitted and decided for this ban. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppealStatus appealStatus = AppealStatus.PENDING;

    @OneToOne(mappedBy = "banRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private Appeal appeal;

    @PrePersist
    public void prePersist() {
        bannedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getUser() { return user; }
    public void setUser(UserProfile user) { this.user = user; }

    public BanTier getTier() { return tier; }
    public void setTier(BanTier tier) { this.tier = tier; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getBannedAt() { return bannedAt; }
    public void setBannedAt(LocalDateTime bannedAt) { this.bannedAt = bannedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public AppealStatus getAppealStatus() { return appealStatus; }
    public void setAppealStatus(AppealStatus appealStatus) { this.appealStatus = appealStatus; }

    public Appeal getAppeal() { return appeal; }
    public void setAppeal(Appeal appeal) { this.appeal = appeal; }
}
