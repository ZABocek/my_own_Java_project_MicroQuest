package com.example.microquest.repository;

import com.example.microquest.model.BanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BanRecordRepository extends JpaRepository<BanRecord, Long> {
    List<BanRecord> findAllByUserIdOrderByBannedAtDesc(Long userId);
    Optional<BanRecord> findTopByUserIdOrderByBannedAtDesc(Long userId);
}
