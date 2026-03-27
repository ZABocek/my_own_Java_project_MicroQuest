package com.example.microquest.controller;

import com.example.microquest.dto.AppealForm;
import com.example.microquest.model.Appeal;
import com.example.microquest.model.AppealStatus;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.AppealRepository;
import com.example.microquest.service.BanService;
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

import java.util.List;

/**
 * Controller for the ban-appeal workflow available to logged-in users.
 * <p>
 * When a user receives a temporary ban they are emailed a link to
 * {@code /appeal/submit/{banRecordId}} where they can write an appeal message.
 * They can then track the status of all their appeals at {@code /appeal/status}.
 * Admin review happens in {@link AdminController}.
 * </p>
 */
@Controller
@RequestMapping("/appeal")
public class AppealController {

    private final BanService banService;
    private final AppealRepository appealRepository;
    private final UserProfileService userProfileService;

    /** All dependencies are constructor-injected by Spring. */
    public AppealController(BanService banService,
                            AppealRepository appealRepository,
                            UserProfileService userProfileService) {
        this.banService = banService;
        this.appealRepository = appealRepository;
        this.userProfileService = userProfileService;
    }

    /**
     * Shows the appeal submission form for a specific ban record.
     * <p>
     * If the user has already submitted an appeal for this ban the form page
     * renders a warning so they know a duplicate cannot be submitted.
     * </p>
     */
    @GetMapping("/submit/{banRecordId}")
    public String showAppealForm(@PathVariable Long banRecordId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        model.addAttribute("banRecordId", banRecordId);
        model.addAttribute("appealForm", new AppealForm());

        // Tell the template whether an appeal already exists so it can warn the user
        boolean hasExisting = banService.getAppealForBan(banRecordId).isPresent();
        model.addAttribute("hasExistingAppeal", hasExisting);
        return "appeal/form";
    }

    /**
     * Handles the appeal submission POST.
     * <p>
     * Validates the form, resolves the current user, delegates to
     * {@link BanService#submitAppeal}, and redirects to the status page on
     * success. Service-layer errors (duplicate appeal, wrong user, etc.) are
     * caught and displayed inline.
     * </p>
     */
    @PostMapping("/submit")
    public String submitAppeal(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute("appealForm") AppealForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("banRecordId", form.getBanRecordId());
            return "appeal/form";
        }
        try {
            UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
            banService.submitAppeal(form.getBanRecordId(), user, form.getMessage());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your appeal has been submitted and will be reviewed by an admin.");
            return "redirect:/appeal/status";
        } catch (Exception e) {
            // Re-display the form with the error from the service layer
            model.addAttribute("banRecordId", form.getBanRecordId());
            model.addAttribute("errorMessage", e.getMessage());
            return "appeal/form";
        }
    }

    /**
     * Shows the current user's full list of submitted appeals ordered by most
     * recent, together with their current status (PENDING / ACCEPTED / REJECTED).
     */
    @GetMapping("/status")
    public String myAppeals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
        List<Appeal> appeals = appealRepository.findAllByUserIdOrderBySubmittedAtDesc(user.getId());
        model.addAttribute("appeals", appeals);
        // Expose enum constants so Thymeleaf can compare without string literals
        model.addAttribute("pendingStatus", AppealStatus.PENDING);
        model.addAttribute("acceptedStatus", AppealStatus.ACCEPTED);
        return "appeal/status";
    }
}
