package com.example.microquest.repository;

import com.example.microquest.model.QuestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestSubmissionRepository extends JpaRepository<QuestSubmission, Long> {
    List<QuestSubmission> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
    List<QuestSubmission> findAllByQuestIdOrderBySubmittedAtDesc(Long questId);
    boolean existsByUserIdAndQuestId(Long userId, Long questId);
    List<QuestSubmission> findAllByUserIdAndQuestIdOrderBySubmittedAtDesc(Long userId, Long questId);
}
