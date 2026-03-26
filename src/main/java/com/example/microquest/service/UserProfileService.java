package com.example.microquest.service;

import com.example.microquest.dto.UserProfileForm;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSave;
import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSaveRepository;
import com.example.microquest.repository.UserLeaderboardRow;
import com.example.microquest.repository.UserProfileRepository;
import com.example.microquest.repository.UserReportRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final QuestRepository questRepository;
    private final QuestSaveRepository questSaveRepository;
    private final UserReportRepository userReportRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              QuestRepository questRepository,
                              QuestSaveRepository questSaveRepository,
                              UserReportRepository userReportRepository) {
        this.userProfileRepository = userProfileRepository;
        this.questRepository = questRepository;
        this.questSaveRepository = questSaveRepository;
        this.userReportRepository = userReportRepository;
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getAllUsers() {
        return userProfileRepository.findAllByOrderByDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public UserProfile getUserOrThrow(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserProfile createUser(UserProfileForm form) {
        String normalizedUsername = form.getUsername().trim().toLowerCase();

        userProfileRepository.findByUsername(normalizedUsername)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
                });

        UserProfile user = new UserProfile();
        user.setUsername(normalizedUsername);
        user.setDisplayName(form.getDisplayName().trim());
        user.setHomeCity(form.getHomeCity() == null ? null : form.getHomeCity().trim());
        user.setBio(form.getBio() == null ? null : form.getBio().trim());
        return userProfileRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserLeaderboardRow> getTopCreators() {
        return userProfileRepository.findTopCreators(PageRequest.of(0, 10));
    }

    @Transactional(readOnly = true)
    public List<Quest> getAuthoredQuests(Long userId) {
        return questRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<QuestSave> getSavedQuests(Long userId) {
        return questSaveRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userProfileRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUsername(String username) {
        return userProfileRepository.findByUsername(username.toLowerCase());
    }

    @Transactional(readOnly = true)
    public UserProfile getByUsername(String username) {
        return userProfileRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void deleteUser(Long id) {
        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be deleted");
        }
        userReportRepository.deleteByReportedUserOrReportingUser(user, user);
        userProfileRepository.delete(user);
    }
    }
}
