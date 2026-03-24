package com.example.microquest.controller;

import com.example.microquest.dto.SubmissionForm;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.SubmissionService;
import com.example.microquest.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quests/{questId}/submit")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserProfileService userProfileService;

    public SubmissionController(SubmissionService submissionService,
                                UserProfileService userProfileService) {
        this.submissionService = submissionService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String showSubmitForm(@PathVariable Long questId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
        boolean alreadySubmitted = submissionService.hasSubmitted(user.getId(), questId);
        model.addAttribute("questId", questId);
        model.addAttribute("submissionForm", new SubmissionForm());
        model.addAttribute("alreadySubmitted", alreadySubmitted);
        return "quests/submit";
    }

    @PostMapping
    public String handleSubmit(@PathVariable Long questId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute("submissionForm") SubmissionForm form,
                               BindingResult bindingResult,
                               @RequestParam("gif") MultipartFile gif,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors() || gif.isEmpty()) {
            model.addAttribute("questId", questId);
            model.addAttribute("gifError", gif.isEmpty() ? "Please choose a GIF file." : null);
            return "quests/submit";
        }

        try {
            UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
            submissionService.submitGif(questId, user, gif, form.getCaption());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Quest completed! Your GIF has been added to your profile.");
            return "redirect:/users/" + user.getId();
        } catch (Exception e) {
            model.addAttribute("questId", questId);
            model.addAttribute("errorMessage", e.getMessage());
            return "quests/submit";
        }
    }
}
