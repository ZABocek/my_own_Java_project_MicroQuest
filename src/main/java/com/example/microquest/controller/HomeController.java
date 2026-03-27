package com.example.microquest.controller;

import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the top-level public pages: home ({@code /}) and about ({@code /about}).
 * <p>
 * The home page shows a snapshot of site activity (quest count, comment count,
 * user count, and the five most recently approved quests) to give new visitors
 * an immediate feel for the community.
 * </p>
 */
@Controller
public class HomeController {

    private final QuestService questService;
    private final UserProfileService userProfileService;

    /** All dependencies are constructor-injected by Spring. */
    public HomeController(QuestService questService, UserProfileService userProfileService) {
        this.questService = questService;
        this.userProfileService = userProfileService;
    }

    /**
     * Renders the home page with site-activity statistics and the five most
     * recently approved quests so returning users can see what is new.
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentQuests", questService.getRecentQuests());
        model.addAttribute("questCount", questService.countQuests());
        model.addAttribute("commentCount", questService.countComments());
        model.addAttribute("userCount", userProfileService.countUsers());
        return "index";
    }

    /** Renders the static about page with information on how the platform works. */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
