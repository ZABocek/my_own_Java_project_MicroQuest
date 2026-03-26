package com.example.microquest.controller;

import com.example.microquest.dto.CommentForm;
import com.example.microquest.dto.QuestForm;
import com.example.microquest.model.Category;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.QuestService;
import com.example.microquest.service.SubmissionService;
import com.example.microquest.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quests")
public class QuestController {

    private final QuestService questService;
    private final UserProfileService userProfileService;
    private final SubmissionService submissionService;

    public QuestController(QuestService questService,
                           UserProfileService userProfileService,
                           SubmissionService submissionService) {
        this.questService = questService;
        this.userProfileService = userProfileService;
        this.submissionService = submissionService;
    }

    @GetMapping
    public String listQuests(@RequestParam(required = false) Category category,
                             @RequestParam(required = false) Difficulty difficulty,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        Page<Quest> questPage = questService.getQuestPage(category, difficulty, page, 6);
        model.addAttribute("questPage", questPage);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDifficulty", difficulty);
        return "quests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        QuestForm form = new QuestForm();
        UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
        form.setAuthorId(currentUser.getId());
        model.addAttribute("questForm", form);
        model.addAttribute("formMode", "create");
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            model.addAttribute("allUsers", userProfileService.getAllUsers());
        }
        return "quests/form";
    }

    @PostMapping
    public String createQuest(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("questForm") QuestForm questForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/auth/login";
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            if (isAdmin) {
                model.addAttribute("allUsers", userProfileService.getAllUsers());
            }
            return "quests/form";
        }
        if (!isAdmin) {
            // Enforce current user as author (prevent form tampering)
            UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
            questForm.setAuthorId(currentUser.getId());
        }
        Quest quest = questService.createQuest(questForm);
        String msg = quest.getStatus().name().equals("APPROVED")
                ? "Quest created and is now live!"
                : "Quest submitted for review. An admin will approve it shortly.";
        redirectAttributes.addFlashAttribute("successMessage", msg);
        return "redirect:/quests/" + quest.getId();
    }

    @GetMapping("/{id}")
    public String showQuest(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Quest quest = questService.getQuestOrThrow(id);
        model.addAttribute("quest", quest);
        model.addAttribute("comments", questService.getCommentsForQuest(id));
        model.addAttribute("commentForm", new CommentForm());

        if (userDetails != null) {
            UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("hasSubmitted",
                    submissionService.hasSubmitted(currentUser.getId(), id));
            // Admins see all submissions; regular users see only their own
            model.addAttribute("submissions", isAdmin
                    ? submissionService.getSubmissionsForQuest(id)
                    : submissionService.getSubmissionsForUserAndQuest(currentUser.getId(), id));
        } else {
            model.addAttribute("hasSubmitted", false);
            model.addAttribute("submissions", java.util.List.of());
        }
        return "quests/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        Quest quest = questService.getQuestOrThrow(id);
        QuestForm form = new QuestForm();
        form.setTitle(quest.getTitle());
        form.setSummary(quest.getSummary());
        form.setDescription(quest.getDescription());
        form.setCategory(quest.getCategory());
        form.setDifficulty(quest.getDifficulty());
        form.setEstimatedMinutes(quest.getEstimatedMinutes());
        form.setIndoor(quest.isIndoor());
        form.setTags(quest.getTags());
        form.setAuthorId(quest.getAuthor().getId());

        model.addAttribute("questId", id);
        model.addAttribute("questForm", form);
        model.addAttribute("formMode", "edit");
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            model.addAttribute("allUsers", userProfileService.getAllUsers());
        }
        return "quests/form";
    }

    @PostMapping("/{id}")
    public String updateQuest(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("questForm") QuestForm questForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/auth/login";
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            // Prevent non-admin users from changing the quest author
            Quest original = questService.getQuestOrThrow(id);
            questForm.setAuthorId(original.getAuthor().getId());
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("questId", id);
            model.addAttribute("formMode", "edit");
            if (isAdmin) {
                model.addAttribute("allUsers", userProfileService.getAllUsers());
            }
            return "quests/form";
        }
        questService.updateQuest(id, questForm);
        redirectAttributes.addFlashAttribute("successMessage", "Quest updated successfully.");
        return "redirect:/quests/" + id;
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("commentForm") CommentForm commentForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (bindingResult.hasErrors()) {
            Quest quest = questService.getQuestOrThrow(id);
            model.addAttribute("quest", quest);
            model.addAttribute("comments", questService.getCommentsForQuest(id));
            return "quests/detail";
        }
        UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
        questService.addCommentByUser(id, currentUser, commentForm.getBody());
        redirectAttributes.addFlashAttribute("successMessage", "Comment added.");
        return "redirect:/quests/" + id;
    }

    @PostMapping("/{id}/save")
    public String saveQuest(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/auth/login";
        UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
        questService.saveQuest(id, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Quest saved to your profile.");
        return "redirect:/quests/" + id;
    }
}
