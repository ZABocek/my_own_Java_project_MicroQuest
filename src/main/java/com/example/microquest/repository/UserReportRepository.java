package com.example.microquest.repository;

import com.example.microquest.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findAllByReviewedFalseOrderByReportedAtDesc();
    List<UserReport> findAllByReportedUserIdOrderByReportedAtDesc(Long userId);
}
