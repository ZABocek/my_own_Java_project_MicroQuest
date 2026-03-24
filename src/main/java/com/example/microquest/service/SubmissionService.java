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

    @Transactional(readOnly = true)
    public List<QuestSubmission> getSubmissionsForUser(Long userId) {
        return submissionRepository.findAllByUserIdOrderBySubmittedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<QuestSubmission> getSubmissionsForQuest(Long questId) {
        return submissionRepository.findAllByQuestIdOrderBySubmittedAtDesc(questId);
    }

    @Transactional(readOnly = true)
    public boolean hasSubmitted(Long userId, Long questId) {
        return submissionRepository.existsByUserIdAndQuestId(userId, questId);
    }
}
