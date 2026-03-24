package com.example.microquest.repository;

import com.example.microquest.model.QuestSave;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestSaveRepository extends JpaRepository<QuestSave, Long> {

    boolean existsByUserIdAndQuestId(Long userId, Long questId);

    List<QuestSave> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByQuestId(Long questId);
}
