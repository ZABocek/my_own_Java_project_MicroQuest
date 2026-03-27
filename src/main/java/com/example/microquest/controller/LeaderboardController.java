package com.example.microquest.controller;

import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the public leaderboard page at {@code /leaderboard}.
 * <p>
 * The leaderboard shows:
 * <ul>
 *   <li>The top 10 most-saved quests (by number of user saves).</li>
 *   <li>The top 10 quest creators (by number of authored quests).</li>
 * </ul>
 * Ranking data is fetched via JPQL projection queries defined in
 * {@link com.example.microquest.repository.QuestRepository} and
 * {@link com.example.microquest.repository.UserProfileRepository}.
 * </p>
 */
@Controller
public class LeaderboardController {

    private final QuestService questService;
    private final UserProfileService userProfileService;

    /** All dependencies are constructor-injected by Spring. */
    public LeaderboardController(QuestService questService, UserProfileService userProfileService) {
        this.questService = questService;
        this.userProfileService = userProfileService;
    }

    /**
     * Renders the leaderboard page with the top-10 most-saved quests and the
     * top-10 quest creators.
     */
    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("topSavedQuests", questService.getTopSavedQuests());
        model.addAttribute("topCreators", userProfileService.getTopCreators());
        return "leaderboard";
    }
}
