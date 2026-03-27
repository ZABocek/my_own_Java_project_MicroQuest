package com.example.microquest.repository;

import com.example.microquest.model.QuestSave;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.QuestSave} entities.
 */
public interface QuestSaveRepository extends JpaRepository<QuestSave, Long> {

    /** Returns {@code true} if the given user has already saved the given quest. */
    boolean existsByUserIdAndQuestId(Long userId, Long questId);

    /** Returns all quests saved by a user, most recently saved first. */
    List<QuestSave> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /** Returns the total number of saves for a quest (used for leaderboard ranking). */
    long countByQuestId(Long questId);
}
