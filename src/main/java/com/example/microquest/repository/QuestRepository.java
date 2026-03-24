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

public interface QuestRepository extends JpaRepository<Quest, Long> {

    // ── Public (approved) queries ─────────────────────────────────────────────

    Page<Quest> findByStatusOrderByCreatedAtDesc(QuestStatus status, Pageable pageable);

    Page<Quest> findByStatusAndCategoryOrderByCreatedAtDesc(QuestStatus status, Category category, Pageable pageable);

    Page<Quest> findByStatusAndDifficultyOrderByCreatedAtDesc(QuestStatus status, Difficulty difficulty, Pageable pageable);

    Page<Quest> findByStatusAndCategoryAndDifficultyOrderByCreatedAtDesc(QuestStatus status, Category category, Difficulty difficulty, Pageable pageable);

    List<Quest> findTop5ByStatusOrderByCreatedAtDesc(QuestStatus status);

    // ── Admin (all statuses) queries ──────────────────────────────────────────

    Page<Quest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Quest> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

    Page<Quest> findByDifficultyOrderByCreatedAtDesc(Difficulty difficulty, Pageable pageable);

    Page<Quest> findByCategoryAndDifficultyOrderByCreatedAtDesc(Category category, Difficulty difficulty, Pageable pageable);

    List<Quest> findTop5ByOrderByCreatedAtDesc();

    List<Quest> findAllByStatusOrderByCreatedAtDesc(QuestStatus status);

    List<Quest> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query("""
            select q.id as questId, q.title as title, count(s.id) as saveCount
            from Quest q
            left join q.saves s
            where q.status = com.example.microquest.model.QuestStatus.APPROVED
            group by q.id, q.title, q.createdAt
            order by count(s.id) desc, q.createdAt desc
            """)
    List<QuestLeaderboardRow> findTopSavedQuests(Pageable pageable);
}

