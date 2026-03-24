package com.example.microquest.controller;

import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeaderboardController {

    private final QuestService questService;
    private final UserProfileService userProfileService;

    public LeaderboardController(QuestService questService, UserProfileService userProfileService) {
        this.questService = questService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("topSavedQuests", questService.getTopSavedQuests());
        model.addAttribute("topCreators", userProfileService.getTopCreators());
        return "leaderboard";
    }
}
