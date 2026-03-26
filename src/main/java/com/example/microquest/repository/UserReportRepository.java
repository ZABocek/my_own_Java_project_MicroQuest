package com.example.microquest.repository;

import com.example.microquest.model.UserProfile;
import com.example.microquest.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findAllByReviewedFalseOrderByReportedAtDesc();
    void deleteByReportedUserOrReportingUser(UserProfile reportedUser, UserProfile reportingUser);
    List<UserReport> findAllByReportedUserIdOrderByReportedAtDesc(Long userId);
}
