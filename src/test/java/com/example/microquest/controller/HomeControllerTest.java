package com.example.microquest.controller;

import com.example.microquest.model.Quest;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean QuestService questService;
    @MockitoBean UserProfileService userProfileService;

    // ── GET / ─────────────────────────────────────────────────────────────────

    @Test
    void home_returns200andIndexView() throws Exception {
        when(questService.getRecentQuests()).thenReturn(Collections.emptyList());
        when(questService.countQuests()).thenReturn(10L);
        when(questService.countComments()).thenReturn(5L);
        when(userProfileService.countUsers()).thenReturn(3L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("questCount", 10L))
                .andExpect(model().attribute("commentCount", 5L))
                .andExpect(model().attribute("userCount", 3L));
    }

    @Test
    void home_populatesRecentQuestsModelAttribute() throws Exception {
        Quest q = new Quest();
        UserProfile author = new UserProfile();
        author.setDisplayName("Test Author");
        q.setAuthor(author);
        when(questService.getRecentQuests()).thenReturn(Collections.singletonList(q));
        when(questService.countQuests()).thenReturn(1L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recentQuests"));
    }

    // ── GET /about ────────────────────────────────────────────────────────────

    @Test
    void about_returns200andAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }
}
