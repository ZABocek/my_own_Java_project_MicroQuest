package com.example.microquest.repository;

import com.example.microquest.model.Category;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.Quest} entities.
 * <p>
 * Methods split into two logical groups:
 * <ul>
 *   <li><b>Public queries</b> — filter by {@code status = APPROVED}; support
 *       optional category / difficulty filters with pagination.</li>
 *   <li><b>Admin queries</b> — return quests of any status for the admin dashboard.</li>
 * </ul>
 * The JPQL {@code @Query} method powers the leaderboard's most-saved-quests ranking.
 * </p>
 */
public interface QuestRepository extends JpaRepository<Quest, Long> {

    // ── Public (approved) queries ─────────────────────────────────────────────

    /** Paginated list of all approved quests ordered newest first. */
    Page<Quest> findByStatusOrderByCreatedAtDesc(QuestStatus status, Pageable pageable);

    /** Approved quests filtered by category only. */
    Page<Quest> findByStatusAndCategoryOrderByCreatedAtDesc(QuestStatus status, Category category, Pageable pageable);

    /** Approved quests filtered by difficulty only. */
    Page<Quest> findByStatusAndDifficultyOrderByCreatedAtDesc(QuestStatus status, Difficulty difficulty, Pageable pageable);

    /** Approved quests filtered by both category and difficulty. */
    Page<Quest> findByStatusAndCategoryAndDifficultyOrderByCreatedAtDesc(QuestStatus status, Category category, Difficulty difficulty, Pageable pageable);

    /** Returns the 5 most recently approved quests for the home-page snapshot. */
    List<Quest> findTop5ByStatusOrderByCreatedAtDesc(QuestStatus status);

    // ── Admin (all statuses) queries ──────────────────────────────────────────

    /** All quests regardless of status, newest first (admin quest management). */
    Page<Quest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** All quests for a given category regardless of status (admin filter). */
    Page<Quest> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

    /** All quests for a given difficulty regardless of status (admin filter). */
    Page<Quest> findByDifficultyOrderByCreatedAtDesc(Difficulty difficulty, Pageable pageable);

    Page<Quest> findByCategoryAndDifficultyOrderByCreatedAtDesc(Category category, Difficulty difficulty, Pageable pageable);

    /** 5 most recent quests of any status (admin dashboard preview). */
    List<Quest> findTop5ByOrderByCreatedAtDesc();

    /** All quests with a specific status; used to count pending quests on the admin dashboard. */
    List<Quest> findAllByStatusOrderByCreatedAtDesc(QuestStatus status);

    /** All quests created by a specific author, newest first (user profile page). */
    List<Quest> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

    /**
     * Ranks approved quests by save count descending (ties broken by creation date).
     * Pass a {@code Pageable} to limit to the top N results for the leaderboard.
     */
            select q.id as questId, q.title as title, count(s.id) as saveCount
            from Quest q
            left join q.saves s
            where q.status = com.example.microquest.model.QuestStatus.APPROVED
            group by q.id, q.title, q.createdAt
            order by count(s.id) desc, q.createdAt desc
            """)
    List<QuestLeaderboardRow> findTopSavedQuests(Pageable pageable);
}

