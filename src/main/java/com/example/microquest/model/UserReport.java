package com.example.microquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private UserProfile reportedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporting_user_id", nullable = false)
    private UserProfile reportingUser;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Column(nullable = false)
    private boolean reviewed = false;

    @PrePersist
    public void prePersist() {
        reportedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getReportedUser() { return reportedUser; }
    public void setReportedUser(UserProfile reportedUser) { this.reportedUser = reportedUser; }

    public UserProfile getReportingUser() { return reportingUser; }
    public void setReportingUser(UserProfile reportingUser) { this.reportingUser = reportingUser; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
}
