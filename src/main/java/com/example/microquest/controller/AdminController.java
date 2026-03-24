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

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final BanService banService;
    private final UserProfileService userProfileService;
    private final UserReportRepository userReportRepository;
    private final FileStorageService fileStorageService;

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

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pendingQuestCount", adminService.countPendingQuests());
        model.addAttribute("pendingAppealsCount", banService.getPendingAppeals().size());
        model.addAttribute("unreviewedReportsCount",
                userReportRepository.findAllByReviewedFalseOrderByReportedAtDesc().size());
        return "admin/dashboard";
    }

    // ── Quest approval ────────────────────────────────────────────────────────

    @GetMapping("/quests")
    public String pendingQuests(Model model) {
        List<Quest> pending = adminService.getPendingQuests();
        model.addAttribute("pendingQuests", pending);
        return "admin/quests";
    }

    @PostMapping("/quests/{id}/approve")
    public String approveQuest(@PathVariable Long id, RedirectAttributes ra) {
        adminService.approveQuest(id);
        ra.addFlashAttribute("successMessage", "Quest approved and is now live.");
        return "redirect:/admin/quests";
    }

    @PostMapping("/quests/{id}/reject")
    public String rejectQuest(@PathVariable Long id,
                              @RequestParam(defaultValue = "Did not meet content guidelines.") String reason,
                              RedirectAttributes ra) {
        adminService.rejectQuest(id, reason);
        ra.addFlashAttribute("successMessage", "Quest rejected.");
        return "redirect:/admin/quests";
    }

    // ── User management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("allUsers", userProfileService.getAllUsers());
        model.addAttribute("banForm", new BanForm());
        return "admin/users";
    }

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

    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes ra) {
        UserProfile user = userProfileService.getUserOrThrow(id);
        banService.liftBan(user);
        ra.addFlashAttribute("successMessage", user.getDisplayName() + " has been unbanned.");
        return "redirect:/admin/users";
    }

    // ── Appeals ───────────────────────────────────────────────────────────────

    @GetMapping("/appeals")
    public String appeals(Model model) {
        List<Appeal> pending = banService.getPendingAppeals();
        model.addAttribute("pendingAppeals", pending);
        return "admin/appeals";
    }

    @PostMapping("/appeals/{id}/accept")
    public String acceptAppeal(@PathVariable Long id,
                               @RequestParam(defaultValue = "Appeal accepted. Your account has been restored.") String adminResponse,
                               RedirectAttributes ra) {
        banService.reviewAppeal(id, true, adminResponse);
        ra.addFlashAttribute("successMessage", "Appeal accepted and ban lifted.");
        return "redirect:/admin/appeals";
    }

    @PostMapping("/appeals/{id}/reject")
    public String rejectAppeal(@PathVariable Long id,
                               @RequestParam(defaultValue = "Appeal reviewed and denied.") String adminResponse,
                               RedirectAttributes ra) {
        banService.reviewAppeal(id, false, adminResponse);
        ra.addFlashAttribute("successMessage", "Appeal rejected.");
        return "redirect:/admin/appeals";
    }

    // ── User reports ──────────────────────────────────────────────────────────

    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("reports", userReportRepository.findAllByReviewedFalseOrderByReportedAtDesc());
        return "admin/reports";
    }

    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(@PathVariable Long id, RedirectAttributes ra) {
        adminService.markReportReviewed(id);
        ra.addFlashAttribute("successMessage", "Report marked as reviewed.");
        return "redirect:/admin/reports";
    }

    // ── Photo ID viewer (admin only) ──────────────────────────────────────────

    @GetMapping("/photo-id/{filename:.+}")
    public ResponseEntity<Resource> viewPhotoId(@PathVariable String filename) throws IOException {
        Path file = fileStorageService.resolvePhotoId(filename);
        Resource resource = new PathResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
