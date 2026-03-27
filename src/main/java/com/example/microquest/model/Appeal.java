package com.example.microquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a ban appeal submitted by a banned user.
 * <p>
 * Each {@link BanRecord} may have at most one {@code Appeal} (enforced by
 * the unique constraint on {@code ban_record_id}).  An admin reviews the
 * appeal and sets the {@code status} to {@code ACCEPTED} or {@code REJECTED},
 * optionally writing an {@code adminResponse}.
 * </p>
 */
@Entity
@Table(name = "appeals")
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ban_record_id", nullable = false, unique = true)
    private BanRecord banRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppealStatus status = AppealStatus.PENDING;

    /** Admin's written response when accepting or rejecting. */
    @Column(length = 1000)
    private String adminResponse;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BanRecord getBanRecord() { return banRecord; }
    public void setBanRecord(BanRecord banRecord) { this.banRecord = banRecord; }

    public UserProfile getUser() { return user; }
    public void setUser(UserProfile user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AppealStatus getStatus() { return status; }
    public void setStatus(AppealStatus status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
