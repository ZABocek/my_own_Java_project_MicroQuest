package com.example.microquest.repository;

import com.example.microquest.model.Appeal;
import com.example.microquest.model.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.Appeal} entities.
 * Provides derived queries for the admin appeals dashboard and for a user's
 * own appeal status page.
 */
public interface AppealRepository extends JpaRepository<Appeal, Long> {
    /** Returns all appeals with the given status, newest first (for the admin queue). */
    List<Appeal> findAllByStatusOrderBySubmittedAtDesc(AppealStatus status);
    /** Looks up the single appeal attached to a specific ban record, if any. */
    Optional<Appeal> findByBanRecordId(Long banRecordId);
    /** Returns all appeals submitted by a specific user, newest first. */
    List<Appeal> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
}
