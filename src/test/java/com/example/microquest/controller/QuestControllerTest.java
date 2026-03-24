package com.example.microquest.controller;

import com.example.microquest.model.Category;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(QuestController.class)
class QuestControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean QuestService questService;
    // Required by ViewModelAdvice which is auto-detected as a @ControllerAdvice
    @MockitoBean UserProfileService userProfileService;

    // ── GET /quests ───────────────────────────────────────────────────────────

    @Test
    void listQuests_noFilters_returns200andListView() throws Exception {
        when(questService.getQuestPage(null, null, 0, 6))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/quests"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/list"))
                .andExpect(model().attributeExists("questPage"));
    }

    @Test
    void listQuests_withCategoryFilter_passesFilterToService() throws Exception {
        when(questService.getQuestPage(eq(Category.FOOD), eq(null), eq(0), eq(6)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/quests").param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/list"));

        verify(questService).getQuestPage(eq(Category.FOOD), eq(null), eq(0), eq(6));
    }

    @Test
    void listQuests_withDifficultyFilter_passesFilterToService() throws Exception {
        when(questService.getQuestPage(eq(null), eq(Difficulty.HARD), eq(0), eq(6)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/quests").param("difficulty", "HARD"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/list"));

        verify(questService).getQuestPage(eq(null), eq(Difficulty.HARD), eq(0), eq(6));
    }

    // ── GET /quests/new ───────────────────────────────────────────────────────

    @Test
    void showCreateForm_returns200andFormView() throws Exception {
        mockMvc.perform(get("/quests/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/form"))
                .andExpect(model().attribute("formMode", "create"))
                .andExpect(model().attributeExists("questForm"));
    }

    // ── POST /quests ──────────────────────────────────────────────────────────

    @Test
    void createQuest_validForm_redirectsToQuestDetail() throws Exception {
        Quest created = questWithId(1L);
        when(questService.createQuest(any())).thenReturn(created);

        mockMvc.perform(post("/quests")
                        .param("title", "Test Quest")
                        .param("summary", "A test summary")
                        .param("description", "Do this and that")
                        .param("category", "OUTDOORS")
                        .param("difficulty", "EASY")
                        .param("estimatedMinutes", "30")
                        .param("authorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests/*"));
    }

    @Test
    void createQuest_blankTitle_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/quests")
                        .param("title", "")
                        .param("summary", "A summary")
                        .param("description", "Details")
                        .param("category", "OUTDOORS")
                        .param("difficulty", "EASY")
                        .param("estimatedMinutes", "30")
                        .param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/form"))
                .andExpect(model().attributeHasFieldErrors("questForm", "title"));
    }

    @Test
    void createQuest_missingRequiredFields_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/quests"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/form"));
    }

    // ── GET /quests/{id} ──────────────────────────────────────────────────────

    @Test
    void showQuest_existingId_returns200andDetailView() throws Exception {
        Quest quest = fullQuest(10L);
        when(questService.getQuestOrThrow(10L)).thenReturn(quest);
        when(questService.getCommentsForQuest(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/quests/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/detail"))
                .andExpect(model().attribute("quest", quest));
    }

    // ── GET /quests/{id}/edit ─────────────────────────────────────────────────

    @Test
    void showEditForm_existingId_returns200andFormView() throws Exception {
        Quest quest = fullQuest(10L);
        when(questService.getQuestOrThrow(10L)).thenReturn(quest);

        mockMvc.perform(get("/quests/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/form"))
                .andExpect(model().attribute("formMode", "edit"))
                .andExpect(model().attribute("questId", 10L));
    }

    // ── POST /quests/{id} ─────────────────────────────────────────────────────

    @Test
    void updateQuest_validForm_redirectsToQuestDetail() throws Exception {
        Quest updated = questWithId(10L);
        when(questService.updateQuest(eq(10L), any())).thenReturn(updated);

        mockMvc.perform(post("/quests/10")
                        .param("title", "Updated Quest")
                        .param("summary", "Updated summary")
                        .param("description", "Updated description")
                        .param("category", "FITNESS")
                        .param("difficulty", "MEDIUM")
                        .param("estimatedMinutes", "60")
                        .param("authorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests/*"));
    }

    @Test
    void updateQuest_invalidForm_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/quests/10")
                        .param("title", "")
                        .param("summary", "Summary")
                        .param("description", "Desc")
                        .param("category", "OUTDOORS")
                        .param("difficulty", "EASY")
                        .param("estimatedMinutes", "30")
                        .param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/form"))
                .andExpect(model().attributeHasFieldErrors("questForm", "title"));
    }

    // ── POST /quests/{id}/comments ────────────────────────────────────────────

    @Test
    void addComment_validForm_redirectsToQuestDetail() throws Exception {
        when(questService.addComment(eq(10L), any())).thenReturn(null);

        mockMvc.perform(post("/quests/10/comments")
                        .param("authorId", "1")
                        .param("body", "Great quest!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests/*"));
    }

    @Test
    void addComment_blankBody_returnsDetailView() throws Exception {
        Quest quest = fullQuest(10L);
        when(questService.getQuestOrThrow(10L)).thenReturn(quest);
        when(questService.getCommentsForQuest(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/quests/10/comments")
                        .param("authorId", "1")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("quests/detail"));
    }

    // ── POST /quests/{id}/save ────────────────────────────────────────────────

    @Test
    void saveQuest_redirectsToQuestDetail() throws Exception {
        mockMvc.perform(post("/quests/10/save").param("userId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests/*"));

        verify(questService).saveQuest(10L, 2L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Quest questWithId(Long id) {
        Quest q = new Quest();
        q.setId(id);
        return q;
    }

    private static Quest fullQuest(Long id) {
        Quest q = new Quest();
        q.setId(id);
        q.setTitle("Sample Quest");
        q.setSummary("A sample quest for testing");
        q.setDescription("Do sample things");
        q.setCategory(Category.OUTDOORS);
        q.setDifficulty(Difficulty.EASY);
        q.setEstimatedMinutes(30);
        q.setIndoor(false);
        UserProfile author = new UserProfile();
        author.setId(1L);
        author.setDisplayName("Test Author");
        author.setUsername("testauthor");
        q.setAuthor(author);
        return q;
    }
}
