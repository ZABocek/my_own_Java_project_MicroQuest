package com.example.microquest.controller;

import com.example.microquest.dto.PasswordChangeForm;
import com.example.microquest.dto.RegistrationForm;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.AuthService;
import com.example.microquest.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserProfileService userProfileService;

    public AuthController(AuthService authService, UserProfileService userProfileService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            @AuthenticationPrincipal UserDetails user,
                            Model model) {
        if (user != null) return "redirect:/";
        if (error != null) model.addAttribute("loginError", "Invalid username or password. Please try again.");
        if (logout != null) model.addAttribute("logoutMessage", "You have been signed out.");
        if (expired != null) model.addAttribute("loginError", "Your session expired. Please sign in again.");
        return "auth/login";
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister(@AuthenticationPrincipal UserDetails user, Model model) {
        if (user != null) return "redirect:/";
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           @RequestParam(required = false) MultipartFile photoId,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(form, photoId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created! Please sign in. Check your email to verify your address.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    // ── Email verification ────────────────────────────────────────────────────

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        boolean ok = authService.verifyEmail(token);
        if (ok) {
            redirectAttributes.addFlashAttribute("successMessage", "Email verified! You're all set.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification link is invalid or has already been used.");
        }
        return "redirect:/auth/login";
    }

    // ── Change password ───────────────────────────────────────────────────────

    @GetMapping("/change-password")
    public String showChangePassword(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }
        try {
            UserProfile currentUser = userProfileService.getByUsername(userDetails.getUsername());
            authService.changePassword(currentUser, form);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
            return "redirect:/users/" + currentUser.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/change-password";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
