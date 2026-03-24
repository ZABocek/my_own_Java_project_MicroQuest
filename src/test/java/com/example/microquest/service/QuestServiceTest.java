package com.example.microquest.service;

import com.example.microquest.dto.CommentForm;
import com.example.microquest.dto.QuestForm;
import com.example.microquest.model.Category;
import com.example.microquest.model.Comment;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSave;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.CommentRepository;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSaveRepository;
import com.example.microquest.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock QuestRepository questRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock CommentRepository commentRepository;
    @Mock QuestSaveRepository questSaveRepository;

    @InjectMocks QuestService questService;

    // ── getQuestPage ──────────────────────────────────────────────────────────

    @Test
    void getQuestPage_noCategoryNoDifficulty_callsFindAll() {
        Page<Quest> expected = new PageImpl<>(Collections.emptyList());
        when(questRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(expected);

        Page<Quest> result = questService.getQuestPage(null, null, 0, 6);

        assertThat(result).isSameAs(expected);
        verify(questRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void getQuestPage_categoryOnly_callsFindByCategory() {
        Page<Quest> expected = new PageImpl<>(Collections.emptyList());
        when(questRepository.findByCategoryOrderByCreatedAtDesc(eq(Category.FOOD), any(Pageable.class)))
                .thenReturn(expected);

        Page<Quest> result = questService.getQuestPage(Category.FOOD, null, 0, 6);

        assertThat(result).isSameAs(expected);
        verify(questRepository).findByCategoryOrderByCreatedAtDesc(eq(Category.FOOD), any(Pageable.class));
    }

    @Test
    void getQuestPage_difficultyOnly_callsFindByDifficulty() {
        Page<Quest> expected = new PageImpl<>(Collections.emptyList());
        when(questRepository.findByDifficultyOrderByCreatedAtDesc(eq(Difficulty.EASY), any(Pageable.class)))
                .thenReturn(expected);

        Page<Quest> result = questService.getQuestPage(null, Difficulty.EASY, 0, 6);

        assertThat(result).isSameAs(expected);
        verify(questRepository).findByDifficultyOrderByCreatedAtDesc(eq(Difficulty.EASY), any(Pageable.class));
    }

    @Test
    void getQuestPage_categoryAndDifficulty_callsFindByCategoryAndDifficulty() {
        Page<Quest> expected = new PageImpl<>(Collections.emptyList());
        when(questRepository.findByCategoryAndDifficultyOrderByCreatedAtDesc(
                eq(Category.FOOD), eq(Difficulty.HARD), any(Pageable.class))).thenReturn(expected);

        Page<Quest> result = questService.getQuestPage(Category.FOOD, Difficulty.HARD, 0, 6);

        assertThat(result).isSameAs(expected);
        verify(questRepository).findByCategoryAndDifficultyOrderByCreatedAtDesc(
                eq(Category.FOOD), eq(Difficulty.HARD), any(Pageable.class));
    }

    // ── getQuestOrThrow ───────────────────────────────────────────────────────

    @Test
    void getQuestOrThrow_existingId_returnsQuest() {
        Quest quest = new Quest();
        when(questRepository.findById(1L)).thenReturn(Optional.of(quest));

        assertThat(questService.getQuestOrThrow(1L)).isSameAs(quest);
    }

    @Test
    void getQuestOrThrow_missingId_throwsNotFound() {
        when(questRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.getQuestOrThrow(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quest not found");
    }

    // ── createQuest ───────────────────────────────────────────────────────────

    @Test
    void createQuest_validForm_savesAndReturnsQuest() {
        UserProfile author = userProfile(1L, "alice");
        QuestForm form = validQuestForm(1L);
        Quest saved = new Quest();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questRepository.save(any(Quest.class))).thenReturn(saved);

        Quest result = questService.createQuest(form);

        assertThat(result).isSameAs(saved);
        verify(questRepository).save(any(Quest.class));
    }

    @Test
    void createQuest_authorNotFound_throwsBadRequest() {
        QuestForm form = validQuestForm(99L);
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.createQuest(form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Author not found");
    }

    @Test
    void createQuest_trimsAllStringFields() {
        UserProfile author = userProfile(1L, "alice");
        QuestForm form = new QuestForm();
        form.setTitle("  Padded Title  ");
        form.setSummary("  short summary  ");
        form.setDescription("  detailed  ");
        form.setCategory(Category.FITNESS);
        form.setDifficulty(Difficulty.MEDIUM);
        form.setEstimatedMinutes(30);
        form.setIndoor(true);
        form.setTags("  yoga, run  ");
        form.setAuthorId(1L);
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

        Quest result = questService.createQuest(form);

        assertThat(result.getTitle()).isEqualTo("Padded Title");
        assertThat(result.getSummary()).isEqualTo("short summary");
        assertThat(result.getDescription()).isEqualTo("detailed");
        assertThat(result.getTags()).isEqualTo("yoga, run");
        assertThat(result.getCategory()).isEqualTo(Category.FITNESS);
        assertThat(result.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(result.isIndoor()).isTrue();
        assertThat(result.getAuthor()).isSameAs(author);
    }

    @Test
    void createQuest_nullTags_setsNullOnQuest() {
        UserProfile author = userProfile(1L, "alice");
        QuestForm form = validQuestForm(1L);
        form.setTags(null);
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

        Quest result = questService.createQuest(form);

        assertThat(result.getTags()).isNull();
    }

    // ── updateQuest ───────────────────────────────────────────────────────────

    @Test
    void updateQuest_validForm_updatesAndReturnsQuest() {
        Quest quest = new Quest();
        UserProfile author = userProfile(1L, "alice");
        QuestForm form = validQuestForm(1L);
        when(questRepository.findById(10L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questRepository.save(quest)).thenReturn(quest);

        Quest result = questService.updateQuest(10L, form);

        assertThat(result).isSameAs(quest);
        assertThat(quest.getTitle()).isEqualTo("My Quest");
    }

    @Test
    void updateQuest_questNotFound_throwsNotFound() {
        when(questRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.updateQuest(99L, validQuestForm(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quest not found");
    }

    @Test
    void updateQuest_authorNotFound_throwsBadRequest() {
        Quest quest = new Quest();
        QuestForm form = validQuestForm(99L);
        when(questRepository.findById(10L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.updateQuest(10L, form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Author not found");
    }

    // ── saveQuest ─────────────────────────────────────────────────────────────

    @Test
    void saveQuest_newSave_createsSave() {
        Quest quest = new Quest();
        UserProfile user = userProfile(2L, "bob");
        when(questRepository.findById(5L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(user));
        when(questSaveRepository.existsByUserIdAndQuestId(2L, 5L)).thenReturn(false);

        questService.saveQuest(5L, 2L);

        verify(questSaveRepository).save(any(QuestSave.class));
    }

    @Test
    void saveQuest_alreadySaved_skipsCreation() {
        Quest quest = new Quest();
        UserProfile user = userProfile(2L, "bob");
        when(questRepository.findById(5L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(user));
        when(questSaveRepository.existsByUserIdAndQuestId(2L, 5L)).thenReturn(true);

        questService.saveQuest(5L, 2L);

        verify(questSaveRepository, never()).save(any(QuestSave.class));
    }

    @Test
    void saveQuest_questNotFound_throwsNotFound() {
        when(questRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.saveQuest(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quest not found");
    }

    @Test
    void saveQuest_userNotFound_throwsBadRequest() {
        Quest quest = new Quest();
        when(questRepository.findById(5L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.saveQuest(5L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    // ── addComment ────────────────────────────────────────────────────────────

    @Test
    void addComment_validForm_savesCommentWithTrimmedBody() {
        Quest quest = new Quest();
        UserProfile author = userProfile(1L, "alice");
        Comment saved = new Comment();
        CommentForm form = new CommentForm();
        form.setAuthorId(1L);
        form.setBody("  Great quest!  ");
        when(questRepository.findById(3L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = questService.addComment(3L, form);

        assertThat(result).isSameAs(saved);
        verify(commentRepository).save(argThat(c -> "Great quest!".equals(c.getBody())));
    }

    @Test
    void addComment_questNotFound_throwsNotFound() {
        CommentForm form = new CommentForm();
        form.setAuthorId(1L);
        form.setBody("hello");
        when(questRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.addComment(99L, form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quest not found");
    }

    @Test
    void addComment_userNotFound_throwsBadRequest() {
        Quest quest = new Quest();
        CommentForm form = new CommentForm();
        form.setAuthorId(99L);
        form.setBody("hello");
        when(questRepository.findById(3L)).thenReturn(Optional.of(quest));
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.addComment(3L, form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    // ── delegation methods ───────────────────────────────────────────────────

    @Test
    void getRecentQuests_returnsRepositoryResult() {
        List<Quest> quests = List.of(new Quest());
        when(questRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(quests);

        assertThat(questService.getRecentQuests()).isSameAs(quests);
    }

    @Test
    void getCommentsForQuest_returnsRepositoryResult() {
        List<Comment> comments = List.of(new Comment());
        when(commentRepository.findAllByQuestIdOrderByCreatedAtDesc(3L)).thenReturn(comments);

        assertThat(questService.getCommentsForQuest(3L)).isSameAs(comments);
    }

    @Test
    void countQuests_returnsRepositoryCount() {
        when(questRepository.count()).thenReturn(42L);

        assertThat(questService.countQuests()).isEqualTo(42L);
    }

    @Test
    void countComments_returnsRepositoryCount() {
        when(commentRepository.count()).thenReturn(7L);

        assertThat(questService.countComments()).isEqualTo(7L);
    }

    // ── helper factory methods ────────────────────────────────────────────────

    private static UserProfile userProfile(Long id, String username) {
        UserProfile u = new UserProfile();
        u.setId(id);
        u.setUsername(username);
        u.setDisplayName(username);
        return u;
    }

    private static QuestForm validQuestForm(Long authorId) {
        QuestForm form = new QuestForm();
        form.setTitle("My Quest");
        form.setSummary("A fine quest");
        form.setDescription("Do this and that");
        form.setCategory(Category.OUTDOORS);
        form.setDifficulty(Difficulty.EASY);
        form.setEstimatedMinutes(45);
        form.setIndoor(false);
        form.setTags("adventure");
        form.setAuthorId(authorId);
        return form;
    }
}
