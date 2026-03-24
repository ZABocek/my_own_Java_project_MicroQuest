package com.example.microquest.repository;

import com.example.microquest.model.Appeal;
import com.example.microquest.model.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {
    List<Appeal> findAllByStatusOrderBySubmittedAtDesc(AppealStatus status);
    Optional<Appeal> findByBanRecordId(Long banRecordId);
    List<Appeal> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
}
