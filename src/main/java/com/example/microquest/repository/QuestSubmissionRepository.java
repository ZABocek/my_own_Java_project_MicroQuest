package com.example.microquest.repository;

import com.example.microquest.model.QuestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.QuestSubmission} entities.
 */
public interface QuestSubmissionRepository extends JpaRepository<QuestSubmission, Long> {
    /** Returns all submissions by a user across all quests, newest first. */
    List<QuestSubmission> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
    /** Returns all submissions for a specific quest (admin view), newest first. */
    List<QuestSubmission> findAllByQuestIdOrderBySubmittedAtDesc(Long questId);
    /** Returns {@code true} if the user has submitted at least once to this quest. */
    boolean existsByUserIdAndQuestId(Long userId, Long questId);
    /** Returns all submissions by a specific user for a specific quest, newest first. */
    List<QuestSubmission> findAllByUserIdAndQuestIdOrderBySubmittedAtDesc(Long userId, Long questId);
}
