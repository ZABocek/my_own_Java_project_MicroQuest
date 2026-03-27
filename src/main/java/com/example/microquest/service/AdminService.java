package com.example.microquest.service;

import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestStatus;
import com.example.microquest.model.UserProfile;
import com.example.microquest.model.UserReport;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.UserProfileRepository;
import com.example.microquest.repository.UserReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for admin-only moderation tasks: quest approval/rejection,
 * user deletion, and user-report management.
 * <p>
 * All mutating methods are transactional.  Email notifications are sent
 * best-effort (failures are silently swallowed so the admin action still completes).
 * </p>
 */
@Service
@Transactional
public class AdminService {

    private final QuestRepository questRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserReportRepository userReportRepository;
    private final EmailService emailService;

    /** All dependencies are constructor-injected by Spring. */
    public AdminService(QuestRepository questRepository,
                        UserProfileRepository userProfileRepository,
                        UserReportRepository userReportRepository,
                        EmailService emailService) {
        this.questRepository = questRepository;
        this.userProfileRepository = userProfileRepository;
        this.userReportRepository = userReportRepository;
        this.emailService = emailService;
    }

    /** Returns all quests currently awaiting admin approval, newest first. */
    @Transactional(readOnly = true)
    public List<Quest> getPendingQuests() {
        return questRepository.findAllByStatusOrderByCreatedAtDesc(QuestStatus.PENDING_APPROVAL);
    }

    /**
     * Approves a quest and notifies the author by email.
     * Sets {@code status} to {@code APPROVED} and persists the change.
     */
    public Quest approveQuest(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found"));
        quest.setStatus(QuestStatus.APPROVED);
        questRepository.save(quest);

        UserProfile author = quest.getAuthor();
        if (author.getEmail() != null) {
            try {
                emailService.sendQuestApprovedEmail(author.getEmail(), author.getDisplayName(), quest.getTitle());
            } catch (Exception ignored) { }
        }
        return quest;
    }

    /**
     * Rejects a quest with an admin-provided reason and notifies the author by email.
     * Sets {@code status} to {@code REJECTED}.
     */
    public Quest rejectQuest(Long questId, String reason) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found"));
        quest.setStatus(QuestStatus.REJECTED);
        questRepository.save(quest);

        UserProfile author = quest.getAuthor();
        if (author.getEmail() != null) {
            try {
                emailService.sendQuestRejectedEmail(author.getEmail(), author.getDisplayName(), quest.getTitle(), reason);
            } catch (Exception ignored) { }
        }
        return quest;
    }

    /**
     * Creates a {@link com.example.microquest.model.UserReport} record for
     * admin review.  The {@code reviewed} flag is initially {@code false}.
     */
    public UserReport fileReport(UserProfile reportingUser, Long reportedUserId, String reason) {
        UserProfile reported = userProfileRepository.findById(reportedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserReport report = new UserReport();
        report.setReportingUser(reportingUser);
        report.setReportedUser(reported);
        report.setReason(reason);
        report.setReportedAt(LocalDateTime.now());
        report.setReviewed(false);
        return userReportRepository.save(report);
    }

    /** Marks a report as reviewed (dismissed) by an admin. */
    public void markReportReviewed(Long reportId) {
        userReportRepository.findById(reportId).ifPresent(r -> {
            r.setReviewed(true);
            userReportRepository.save(r);
        });
    }

    /** Returns the count of quests currently awaiting approval (for the admin dashboard badge). */
    @Transactional(readOnly = true)
    public long countPendingQuests() {
        return questRepository.findAllByStatusOrderByCreatedAtDesc(QuestStatus.PENDING_APPROVAL).size();
    }
}
