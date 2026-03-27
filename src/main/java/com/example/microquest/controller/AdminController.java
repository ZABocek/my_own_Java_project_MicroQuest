package com.example.microquest.controller;

import com.example.microquest.dto.BanForm;
import com.example.microquest.model.Appeal;
import com.example.microquest.model.Quest;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.UserReportRepository;
import com.example.microquest.service.AdminService;
import com.example.microquest.service.BanService;
import com.example.microquest.service.FileStorageService;
import com.example.microquest.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * HTTP controller for all admin-only operations.
 * <p>
 * Every endpoint in this controller is guarded at two levels:
 * <ol>
 *   <li>URL-level: Spring Security's filter chain restricts {@code /admin/**}
 *       to principals with {@code ROLE_ADMIN} (see {@code SecurityConfig}).</li>
 *   <li>Method-level: {@code @PreAuthorize("hasAuthority('ROLE_ADMIN')")} provides
 *       a defence-in-depth check so that even if the URL rule were misconfigured,
 *       non-admins cannot invoke the handler.</li>
 * </ol>
 * Responsibilities covered here:
 * <ul>
 *   <li>Admin dashboard (a summary of pending items)</li>
 *   <li>Quest approval / rejection workflow</li>
 *   <li>User ban / unban / delete</li>
 *   <li>Ban-appeal review (accept or reject)</li>
 *   <li>User-report queue (dismiss reviewed reports)</li>
 *   <li>Secure photo-ID file viewer</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final BanService banService;
    private final UserProfileService userProfileService;
    private final UserReportRepository userReportRepository;
    private final FileStorageService fileStorageService;

    /** All dependencies are constructor-injected by Spring. */
    public AdminController(AdminService adminService,
                           BanService banService,
                           UserProfileService userProfileService,
                           UserReportRepository userReportRepository,
                           FileStorageService fileStorageService) {
        this.adminService = adminService;
        this.banService = banService;
        this.userProfileService = userProfileService;
        this.userReportRepository = userReportRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Renders the admin dashboard with counts of pending quests, pending appeals,
     * and unreviewed user reports so the admin can see at a glance what needs
     * attention.
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pendingQuestCount", adminService.countPendingQuests());
        model.addAttribute("pendingAppealsCount", banService.getPendingAppeals().size());
        model.addAttribute("unreviewedReportsCount",
                userReportRepository.findAllByReviewedFalseOrderByReportedAtDesc().size());
        return "admin/dashboard";
    }

    // ── Quest approval ────────────────────────────────────────────────────────

    /** Lists all quests currently in {@code PENDING_APPROVAL} status for admin review. */
    @GetMapping("/quests")
    public String pendingQuests(Model model) {
        List<Quest> pending = adminService.getPendingQuests();
        model.addAttribute("pendingQuests", pending);
        return "admin/quests";
    }

    /**
     * Approves a quest — sets its status to {@code APPROVED} and notifies the
     * author by email (best-effort).
     */
    @PostMapping("/quests/{id}/approve")
    public String approveQuest(@PathVariable Long id, RedirectAttributes ra) {
        adminService.approveQuest(id);
        ra.addFlashAttribute("successMessage", "Quest approved and is now live.");
        return "redirect:/admin/quests";
    }

    /**
     * Rejects a quest with an optional reason that is emailed to the author so
     * they understand what changes are needed before resubmitting.
     */
    @PostMapping("/quests/{id}/reject")
    public String rejectQuest(@PathVariable Long id,
                              @RequestParam(defaultValue = "Did not meet content guidelines.") String reason,
                              RedirectAttributes ra) {
        adminService.rejectQuest(id, reason);
        ra.addFlashAttribute("successMessage", "Quest rejected.");
        return "redirect:/admin/quests";
    }

    // ── User management ───────────────────────────────────────────────────────

    /** Loads the user-management page with all registered users and an empty ban form. */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("allUsers", userProfileService.getAllUsers());
        model.addAttribute("banForm", new BanForm());
        return "admin/users";
    }

    /**
     * Issues a ban against the user using tiered escalation in {@link BanService#issueBan}.
     * Validation errors redirect back with an error flash.
     */
    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id,
                          @Valid @ModelAttribute("banForm") BanForm banForm,
                          BindingResult bindingResult,
                          RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", "Ban reason is required.");
            return "redirect:/admin/users";
        }
        UserProfile user = userProfileService.getUserOrThrow(id);
        banService.issueBan(user, banForm.getReason());
        ra.addFlashAttribute("successMessage", "Ban issued for " + user.getDisplayName() + ".");
        return "redirect:/admin/users";
    }

    /**
     * Lifts an active ban, restores the {@code active} flag, and clears the
     * {@code bannedUntil} timestamp so the user can log in immediately.
     */
    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes ra) {
        UserProfile user = userProfileService.getUserOrThrow(id);
        banService.liftBan(user);
        ra.addFlashAttribute("successMessage", user.getDisplayName() + " has been unbanned.");
        return "redirect:/admin/users";
    }

    /**
     * Permanently deletes a non-admin user account. Admin accounts are protected
     * from deletion. Any uploaded photo-ID file is removed from disk first.
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        UserProfile user = userProfileService.getUserOrThrow(id);
        // Guard: admin accounts must not be deleted through this endpoint
        if (user.getRole().name().equals("ROLE_ADMIN")) {
            ra.addFlashAttribute("errorMessage", "Admin accounts cannot be deleted.");
            return "redirect:/admin/users";
        }
        String displayName = user.getDisplayName();
        // Best-effort photo-ID cleanup — failure is silently ignored
        if (user.getPhotoIdPath() != null) {
            try { fileStorageService.deletePhotoId(user.getPhotoIdPath()); } catch (java.io.IOException ignored) {}
        }
        userProfileService.deleteUser(id);
        ra.addFlashAttribute("successMessage", "User \"" + displayName + "\" has been permanently deleted.");
        return "redirect:/admin/users";
    }

    // ── Appeals ───────────────────────────────────────────────────────────────

    /** Lists all ban appeals currently in {@code PENDING} status awaiting admin review. */
    @GetMapping("/appeals")
    public String appeals(Model model) {
        List<Appeal> pending = banService.getPendingAppeals();
        model.addAttribute("pendingAppeals", pending);
        return "admin/appeals";
    }

    /**
     * Accepts a ban appeal: lifts the ban, decrements the ban counter, and
     * sends an acceptance decision email to the user.
     */
    @PostMapping("/appeals/{id}/accept")
    public String acceptAppeal(@PathVariable Long id,
                               @RequestParam(defaultValue = "Appeal accepted. Your account has been restored.") String adminResponse,
                               RedirectAttributes ra) {
        banService.reviewAppeal(id, true, adminResponse);
        ra.addFlashAttribute("successMessage", "Appeal accepted and ban lifted.");
        return "redirect:/admin/appeals";
    }

    /**
     * Rejects a ban appeal while keeping the ban in place. The {@code adminResponse}
     * message is stored on the appeal and emailed to the user.
     */
    @PostMapping("/appeals/{id}/reject")
    public String rejectAppeal(@PathVariable Long id,
                               @RequestParam(defaultValue = "Appeal reviewed and denied.") String adminResponse,
                               RedirectAttributes ra) {
        banService.reviewAppeal(id, false, adminResponse);
        ra.addFlashAttribute("successMessage", "Appeal rejected.");
        return "redirect:/admin/appeals";
    }

    // ── User reports ──────────────────────────────────────────────────────────

    /** Shows all unreviewed user reports ordered by most-recently filed. */
    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("reports", userReportRepository.findAllByReviewedFalseOrderByReportedAtDesc());
        return "admin/reports";
    }

    /**
     * Marks a report as reviewed so it no longer appears in the unreviewed queue.
     * The report record is retained for the audit trail.
     */
    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(@PathVariable Long id, RedirectAttributes ra) {
        adminService.markReportReviewed(id);
        ra.addFlashAttribute("successMessage", "Report marked as reviewed.");
        return "redirect:/admin/reports";
    }

    // ── Photo ID viewer (admin only) ──────────────────────────────────────────

    /**
     * Streams a photo-ID image to the admin browser inline.
     * Path-traversal attacks are mitigated by {@link FileStorageService#resolvePhotoId}
     * which asserts the resolved path stays within the {@code uploads/photo-ids/} directory.
     */
    @GetMapping("/photo-id/{filename:.+}")
    public ResponseEntity<Resource> viewPhotoId(@PathVariable String filename) throws IOException {
        Path file = fileStorageService.resolvePhotoId(filename);
        Resource resource = new PathResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        // Determine content type from file extension for correct browser rendering
        String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
