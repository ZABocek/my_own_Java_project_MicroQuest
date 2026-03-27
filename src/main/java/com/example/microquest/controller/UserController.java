package com.example.microquest.controller;

import com.example.microquest.dto.ReportUserForm;
import com.example.microquest.dto.UserProfileForm;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.AdminService;
import com.example.microquest.service.BanService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for user-profile pages under {@code /users}.
 * <p>
 * Provides: listing all users, viewing a user's public profile (authored
 * quests, saved quests, GIF submissions, ban history), editing a profile,
 * and submitting a report against another user.
 * </p>
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserProfileService userProfileService;
    private final SubmissionService submissionService;
    private final BanService banService;
    private final AdminService adminService;

    /** All dependencies are constructor-injected by Spring. */
    public UserController(UserProfileService userProfileService,
                          SubmissionService submissionService,
                          BanService banService,
                          AdminService adminService) {
        this.userProfileService = userProfileService;
        this.submissionService = submissionService;
        this.banService = banService;
        this.adminService = adminService;
    }

    /** Lists all registered users (admin view, linked from the admin dashboard). */
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userProfileService.getAllUsers());
        return "users/list";
    }

    /** Shows the user-creation form (admin utility; regular registration goes through AuthController). */
    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("userProfileForm", new UserProfileForm());
        return "users/form";
    }

    /** Processes the user-creation form POST and redirects to the new profile page. */
    @PostMapping
    public String createUser(@Valid @ModelAttribute("userProfileForm") UserProfileForm userProfileForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "users/form";
        }
        var user = userProfileService.createUser(userProfileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Profile created.");
        return "redirect:/users/" + user.getId();
    }

    /**
     * Renders a user's public profile page.
     * Flags {@code isOwnProfile} and {@code isAdmin} so the template can
     * conditionally render edit/report controls.
     */
    @GetMapping("/{id}")
    public String showUser(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        UserProfile user = userProfileService.getUserOrThrow(id);
        model.addAttribute("user", user);
        model.addAttribute("authoredQuests", userProfileService.getAuthoredQuests(id));
        model.addAttribute("savedQuests", userProfileService.getSavedQuests(id));
        model.addAttribute("submissions", submissionService.getSubmissionsForUser(id));
        model.addAttribute("banHistory", banService.getBanHistoryForUser(id));
        model.addAttribute("reportForm", new ReportUserForm());

        boolean isOwnProfile = userDetails != null
                && userDetails.getUsername().equalsIgnoreCase(user.getUsername());
        boolean isAdmin = userDetails != null
                && userDetails.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("isAdmin", isAdmin);
        return "users/detail";
    }

    /**
     * Submits a community report against another user.
     * Delegates to {@link AdminService#fileReport} which creates a
     * {@link com.example.microquest.model.UserReport} record for admin review.
     */
    @PostMapping("/{id}/report")
    public String reportUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("reportForm") ReportUserForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/auth/login";
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report reason is required.");
            return "redirect:/users/" + id;
        }
        try {
            UserProfile reporter = userProfileService.getByUsername(userDetails.getUsername());
            adminService.fileReport(reporter, id, form.getReason());
            redirectAttributes.addFlashAttribute("successMessage", "Report submitted. Thank you.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/" + id;
    }
}
