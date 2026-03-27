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

/**
 * Service for user-profile queries and management.
 * <p>
 * Provides: listing users, fetching authored/saved quests, the leaderboard
 * top-creators query, user creation (admin util), and safe user deletion
 * (guards against deleting admin accounts and cleans up report records).
 * </p>
 */
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

    /** Returns all users sorted by display name (for admin user-list page). */
    @Transactional(readOnly = true)
    public List<UserProfile> getAllUsers() {
        return userProfileRepository.findAllByOrderByDisplayNameAsc();
    }

    /** Fetches a user by ID or throws 404 if not found. */
    @Transactional(readOnly = true)
    public UserProfile getUserOrThrow(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Creates a new user profile (admin utility; no password hash set).
     * Normalises username to lower-case and checks for duplicates.
     */
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

    /** Returns the top-10 quest creators for the leaderboard. */
    @Transactional(readOnly = true)
    public List<UserLeaderboardRow> getTopCreators() {
        return userProfileRepository.findTopCreators(PageRequest.of(0, 10));
    }

    /** Returns all quests authored by a user, newest first (for their profile page). */
    @Transactional(readOnly = true)
    public List<Quest> getAuthoredQuests(Long userId) {
        return questRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId);
    }

    /** Returns all quests saved (bookmarked) by a user, most recently saved first. */
    @Transactional(readOnly = true)
    public List<QuestSave> getSavedQuests(Long userId) {
        return questSaveRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Total number of registered users (for the home-page counter). */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userProfileRepository.count();
    }

    /** Looks up a user by username (case-insensitive); returns empty if not found. */
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUsername(String username) {
        return userProfileRepository.findByUsername(username.toLowerCase());
    }

    /** Looks up a user by username (case-insensitive); throws 404 if not found. */
    @Transactional(readOnly = true)
    public UserProfile getByUsername(String username) {
        return userProfileRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Deletes a user account and all their associated report records.
     * Throws 403 if the target account is an admin (admins cannot be deleted via this path).
     */
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
