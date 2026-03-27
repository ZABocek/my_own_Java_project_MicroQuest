package com.example.microquest.repository;

import com.example.microquest.model.BanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.BanRecord} entities.
 */
public interface BanRecordRepository extends JpaRepository<BanRecord, Long> {
    /** Returns the full ban history for a user, most recent first. */
    List<BanRecord> findAllByUserIdOrderByBannedAtDesc(Long userId);
    /** Returns the most recent ban record for a user (used to check the current active ban). */
    Optional<BanRecord> findTopByUserIdOrderByBannedAtDesc(Long userId);
}
