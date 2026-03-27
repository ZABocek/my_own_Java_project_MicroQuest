package com.example.microquest.repository;

import com.example.microquest.model.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.Comment} entities.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Returns all comments for a quest, newest first (for the quest detail page). */
    List<Comment> findAllByQuestIdOrderByCreatedAtDesc(Long questId);

    /** Returns the total number of comments on a single quest. */
    long countByQuestId(Long questId);
}
