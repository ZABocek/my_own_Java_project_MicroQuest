package com.example.microquest.service;

import com.example.microquest.dto.UserProfileForm;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSave;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSaveRepository;
import com.example.microquest.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock UserProfileRepository userProfileRepository;
    @Mock QuestRepository questRepository;
    @Mock QuestSaveRepository questSaveRepository;

    @InjectMocks UserProfileService userProfileService;

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsRepositoryResult() {
        List<UserProfile> users = List.of(new UserProfile());
        when(userProfileRepository.findAllByOrderByDisplayNameAsc()).thenReturn(users);

        assertThat(userProfileService.getAllUsers()).isSameAs(users);
    }

    // ── getUserOrThrow ────────────────────────────────────────────────────────

    @Test
    void getUserOrThrow_existingId_returnsUser() {
        UserProfile user = new UserProfile();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userProfileService.getUserOrThrow(1L)).isSameAs(user);
    }

    @Test
    void getUserOrThrow_missingId_throwsNotFound() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getUserOrThrow(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_newUsername_savesWithNormalizedFields() {
        UserProfileForm form = new UserProfileForm();
        form.setUsername("  Alice  ");
        form.setDisplayName("  Alice Display  ");
        form.setHomeCity("  New York  ");
        form.setBio("  Loves adventures.  ");
        when(userProfileRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile result = userProfileService.createUser(form);

        // username trimmed and lowercased
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getDisplayName()).isEqualTo("Alice Display");
        assertThat(result.getHomeCity()).isEqualTo("New York");
        assertThat(result.getBio()).isEqualTo("Loves adventures.");
    }

    @Test
    void createUser_duplicateUsername_throwsBadRequest() {
        UserProfileForm form = new UserProfileForm();
        form.setUsername("alice");
        form.setDisplayName("Alice");
        when(userProfileRepository.findByUsername("alice")).thenReturn(Optional.of(new UserProfile()));

        assertThatThrownBy(() -> userProfileService.createUser(form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_nullOptionalFields_setsNull() {
        UserProfileForm form = new UserProfileForm();
        form.setUsername("bob");
        form.setDisplayName("Bob");
        form.setHomeCity(null);
        form.setBio(null);
        when(userProfileRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile result = userProfileService.createUser(form);

        assertThat(result.getHomeCity()).isNull();
        assertThat(result.getBio()).isNull();
    }

    @Test
    void createUser_savesViaRepository() {
        UserProfileForm form = new UserProfileForm();
        form.setUsername("carol");
        form.setDisplayName("Carol");
        when(userProfileRepository.findByUsername("carol")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        userProfileService.createUser(form);

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    // ── delegation methods ────────────────────────────────────────────────────

    @Test
    void getAuthoredQuests_returnsRepositoryResult() {
        List<Quest> quests = List.of(new Quest());
        when(questRepository.findAllByAuthorIdOrderByCreatedAtDesc(1L)).thenReturn(quests);

        assertThat(userProfileService.getAuthoredQuests(1L)).isSameAs(quests);
    }

    @Test
    void getSavedQuests_returnsRepositoryResult() {
        List<QuestSave> saves = List.of(new QuestSave());
        when(questSaveRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).thenReturn(saves);

        assertThat(userProfileService.getSavedQuests(1L)).isSameAs(saves);
    }

    @Test
    void countUsers_returnsRepositoryCount() {
        when(userProfileRepository.count()).thenReturn(5L);

        assertThat(userProfileService.countUsers()).isEqualTo(5L);
    }
}
