package com.example.microquest.controller;

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

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean QuestService questService;
    @MockitoBean UserProfileService userProfileService;

    // ── GET /leaderboard ──────────────────────────────────────────────────────

    @Test
    void leaderboard_returns200andLeaderboardView() throws Exception {
        when(questService.getTopSavedQuests()).thenReturn(Collections.emptyList());
        when(userProfileService.getTopCreators()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attributeExists("topSavedQuests"))
                .andExpect(model().attributeExists("topCreators"));
    }

    @Test
    void leaderboard_populatesTopSavedQuestsAndTopCreators() throws Exception {
        when(questService.getTopSavedQuests()).thenReturn(Collections.emptyList());
        when(userProfileService.getTopCreators()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("topSavedQuests", Collections.emptyList()))
                .andExpect(model().attribute("topCreators", Collections.emptyList()));
    }
}
