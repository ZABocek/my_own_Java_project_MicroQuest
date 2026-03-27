package com.example.microquest.service;

import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSubmission;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSubmissionRepository;
import com.example.microquest.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Service for quest GIF/media submissions.
 * <p>
 * Handles file storage delegation to {@link FileStorageService}, access-control
 * enforcement (only the submitter or an admin may view/delete a submission),
 * and submission lifecycle queries.
 * </p>
 */
@Service
@Transactional
public class SubmissionService {

    private final QuestSubmissionRepository submissionRepository;
    private final QuestRepository questRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileStorageService fileStorageService;

    public SubmissionService(QuestSubmissionRepository submissionRepository,
                             QuestRepository questRepository,
                             UserProfileRepository userProfileRepository,
                             FileStorageService fileStorageService) {
        this.submissionRepository = submissionRepository;
        this.questRepository = questRepository;
        this.userProfileRepository = userProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Stores the uploaded media file and creates a {@link com.example.microquest.model.QuestSubmission}
     * record linking it to the quest and the user.
     * Throws 400 if the file is invalid (wrong type, too large, or IO error).
     */
    public QuestSubmission submitGif(Long questId, UserProfile user, MultipartFile gif, String caption) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found"));

        String gifFilename;
        try {
            gifFilename = fileStorageService.storeGif(gif, user.getId());
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        QuestSubmission sub = new QuestSubmission();
        sub.setQuest(quest);
        sub.setUser(user);
        sub.setGifPath(gifFilename);
        sub.setCaption(caption != null ? caption.trim() : "");
        return submissionRepository.save(sub);
    }

    /** Returns all submissions by a user across all quests, newest first. */
    @Transactional(readOnly = true)
    public List<QuestSubmission> getSubmissionsForUser(Long userId) {
        return submissionRepository.findAllByUserIdOrderBySubmittedAtDesc(userId);
    }

    /** Returns all submissions for a quest (used by the admin view). */
    @Transactional(readOnly = true)
    public List<QuestSubmission> getSubmissionsForQuest(Long questId) {
        return submissionRepository.findAllByQuestIdOrderBySubmittedAtDesc(questId);
    }

    /** Returns {@code true} if the user has submitted at least once. */
    @Transactional(readOnly = true)
    public boolean hasSubmitted(Long userId, Long questId) {
        return submissionRepository.existsByUserIdAndQuestId(userId, questId);
    }

    /** Returns a user's own submissions for a specific quest, newest first. */
    @Transactional(readOnly = true)
    public List<QuestSubmission> getSubmissionsForUserAndQuest(Long userId, Long questId) {
        return submissionRepository.findAllByUserIdAndQuestIdOrderBySubmittedAtDesc(userId, questId);
    }

    /**
     * Fetches a submission, enforcing access-control: non-admins can only
     * access their own submissions.  Throws 404 if not found, 403 if denied.
     */
    @Transactional(readOnly = true)
    public QuestSubmission getSubmissionSecure(Long submissionId, Long requestingUserId, boolean isAdmin) {
        QuestSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (!isAdmin && !sub.getUser().getId().equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return sub;
    }

    /**
     * Deletes a submission and its stored media file.
     * Throws 403 if the requesting user is not the submitter and is not an admin.
     * File deletion is best-effort: IO errors are ignored (file may already be gone).
     */
    public void deleteSubmission(Long submissionId, Long requestingUserId, boolean isAdmin) {
        QuestSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (!isAdmin && !sub.getUser().getId().equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own submissions");
        }
        try {
            fileStorageService.deleteGif(sub.getGifPath());
        } catch (IOException e) {
            // best-effort: file may already be gone
        }
        submissionRepository.delete(sub);
    }
}
