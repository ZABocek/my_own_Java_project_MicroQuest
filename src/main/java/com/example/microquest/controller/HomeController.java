package com.example.microquest.controller;

import com.example.microquest.service.QuestService;
import com.example.microquest.service.UserProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final QuestService questService;
    private final UserProfileService userProfileService;

    public HomeController(QuestService questService, UserProfileService userProfileService) {
        this.questService = questService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentQuests", questService.getRecentQuests());
        model.addAttribute("questCount", questService.countQuests());
        model.addAttribute("commentCount", questService.countComments());
        model.addAttribute("userCount", userProfileService.countUsers());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
