package com.example.microquest.repository;

import com.example.microquest.model.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByQuestIdOrderByCreatedAtDesc(Long questId);

    long countByQuestId(Long questId);
}
