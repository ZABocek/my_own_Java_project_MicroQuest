package com.example.microquest.service;

import com.example.microquest.dto.CommentForm;
import com.example.microquest.dto.QuestForm;
import com.example.microquest.model.Category;
import com.example.microquest.model.Comment;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSave;
import com.example.microquest.model.QuestStatus;
import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.CommentRepository;
import com.example.microquest.repository.QuestLeaderboardRow;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSaveRepository;
import com.example.microquest.repository.UserProfileRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Core business-logic service for quest management.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Paginated, filterable quest retrieval for the public list view.</li>
 *   <li>Quest creation (with automatic approval for admin authors) and editing.</li>
 *   <li>Quest bookmarking (saves) with duplicate-save protection.</li>
 *   <li>Comment creation.</li>
 *   <li>Aggregate counts for the home-page statistics panel.</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class QuestService {

    private final QuestRepository questRepository;
    private final UserProfileRepository userProfileRepository;
    private final CommentRepository commentRepository;
    private final QuestSaveRepository questSaveRepository;

    public QuestService(QuestRepository questRepository,
                        UserProfileRepository userProfileRepository,
                        CommentRepository commentRepository,
                        QuestSaveRepository questSaveRepository) {
        this.questRepository = questRepository;
        this.userProfileRepository = userProfileRepository;
        this.commentRepository = commentRepository;
        this.questSaveRepository = questSaveRepository;
    }

    /**
     * Returns a paginated, optionally filtered page of approved quests.
     * Dispatches to the appropriate repository query based on which filters
     * ({@code category}, {@code difficulty}, both, or neither) are provided.
     */
    @Transactional(readOnly = true)
    public Page<Quest> getQuestPage(Category category, Difficulty difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        QuestStatus approved = QuestStatus.APPROVED;
        if (category != null && difficulty != null) {
            return questRepository.findByStatusAndCategoryAndDifficultyOrderByCreatedAtDesc(approved, category, difficulty, pageable);
        }
        if (category != null) {
            return questRepository.findByStatusAndCategoryOrderByCreatedAtDesc(approved, category, pageable);
        }
        if (difficulty != null) {
            return questRepository.findByStatusAndDifficultyOrderByCreatedAtDesc(approved, difficulty, pageable);
        }
        return questRepository.findByStatusOrderByCreatedAtDesc(approved, pageable);
    }

    /** Fetches a quest by ID or throws 404 if not found. */
    @Transactional(readOnly = true)
    public Quest getQuestOrThrow(Long id) {
        return questRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found"));
    }

    /**
     * Creates a new quest from the form data.
     * Admin-authored quests are immediately {@code APPROVED};
     * regular-user quests start as {@code PENDING_APPROVAL}.
     */
    public Quest createQuest(QuestForm form) {
        UserProfile author = userProfileRepository.findById(form.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author not found"));

        Quest quest = new Quest();
        applyForm(quest, form, author);
        // Admins get instant approval; regular users go through review
        if (author.getRole() == Role.ROLE_ADMIN) {
            quest.setStatus(QuestStatus.APPROVED);
        } else {
            quest.setStatus(QuestStatus.PENDING_APPROVAL);
        }
        return questRepository.save(quest);
    }

    /** Updates an existing quest's fields from the form data. Preserves current status. */
    public Quest updateQuest(Long id, QuestForm form) {
        Quest quest = getQuestOrThrow(id);
        UserProfile author = userProfileRepository.findById(form.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author not found"));
        applyForm(quest, form, author);
        return questRepository.save(quest);
    }

    /**
     * Bookmarks a quest for a user.  If the user has already saved the quest,
     * this is a no-op (idempotent).
     */
    public void saveQuest(Long questId, Long userId) {
        Quest quest = getQuestOrThrow(questId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!questSaveRepository.existsByUserIdAndQuestId(userId, questId)) {
            QuestSave save = new QuestSave();
            save.setQuest(quest);
            save.setUser(user);
            questSaveRepository.save(save);
        }
    }

    public Comment addComment(Long questId, CommentForm form) {
        Quest quest = getQuestOrThrow(questId);
        UserProfile author = userProfileRepository.findById(form.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        Comment comment = new Comment();
        comment.setQuest(quest);
        comment.setAuthor(author);
        comment.setBody(form.getBody().trim());
        return commentRepository.save(comment);
    }

    /** Returns the 5 most recently approved quests for the home-page snapshot. */
    @Transactional(readOnly = true)
    public List<Quest> getRecentQuests() {
        return questRepository.findTop5ByStatusOrderByCreatedAtDesc(QuestStatus.APPROVED);
    }

    /** Add a comment using an already-resolved author profile (current logged-in user). */
    public Comment addCommentByUser(Long questId, UserProfile author, String body) {
        Quest quest = getQuestOrThrow(questId);
        Comment comment = new Comment();
        comment.setQuest(quest);
        comment.setAuthor(author);
        comment.setBody(body.trim());
        return commentRepository.save(comment);
    }

    /** Returns the top-10 most-saved approved quests for the leaderboard. */
    @Transactional(readOnly = true)
    public List<QuestLeaderboardRow> getTopSavedQuests() {
        return questRepository.findTopSavedQuests(PageRequest.of(0, 10));
    }

    /** Returns all comments for a quest, newest first. */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForQuest(Long questId) {
        return commentRepository.findAllByQuestIdOrderByCreatedAtDesc(questId);
    }

    /** Total number of quests in the database (for the home-page counter). */
    @Transactional(readOnly = true)
    public long countQuests() {
        return questRepository.count();
    }

    /** Total number of comments across all quests (for the home-page counter). */
    @Transactional(readOnly = true)
    public long countComments() {
        return commentRepository.count();
    }

    /** Internal helper: copies form field values onto the Quest entity. */
    private void applyForm(Quest quest, QuestForm form, UserProfile author) {
        quest.setTitle(form.getTitle().trim());
        quest.setSummary(form.getSummary().trim());
        quest.setDescription(form.getDescription().trim());
        quest.setCategory(form.getCategory());
        quest.setDifficulty(form.getDifficulty());
        quest.setEstimatedMinutes(form.getEstimatedMinutes());
        quest.setIndoor(form.isIndoor());
        quest.setTags(form.getTags() == null ? null : form.getTags().trim());
        quest.setAuthor(author);
    }
}
