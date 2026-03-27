package com.example.microquest.config;

import com.example.microquest.model.Category;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.UserProfile;
import com.example.microquest.service.UserProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * Cross-cutting Thymeleaf view-model provider.
 * <p>
 * Spring invokes every {@code @ModelAttribute} method here before rendering
 * <em>any</em> controller's Thymeleaf template.  This avoids duplicating common
 * look-ups (e.g. enum values, the currently logged-in user) in every controller.
 * </p>
 * <ul>
 *   <li>{@code categories}  — all {@link Category} enum values for filter dropdowns.</li>
 *   <li>{@code difficulties} — all {@link Difficulty} enum values for filter dropdowns.</li>
 *   <li>{@code currentUser} — the fully resolved {@link UserProfile} of the logged-in
 *       user, or {@code null} for anonymous visitors.</li>
 *   <li>{@code isAdmin}     — {@code true} when the current principal holds
 *       {@code ROLE_ADMIN}.</li>
 * </ul>
 */
@ControllerAdvice
public class ViewModelAdvice {

    private final UserProfileService userProfileService;

    /** Constructor-injected service used to resolve the logged-in user profile. */
    public ViewModelAdvice(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Exposes all {@link Category} enum constants to every Thymeleaf template
     * under the key {@code categories} (used by quest filter dropdowns).
     */
    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    /**
     * Exposes all {@link Difficulty} enum constants to every Thymeleaf template
     * under the key {@code difficulties} (used by quest filter dropdowns).
     */
    @ModelAttribute("difficulties")
    public Difficulty[] difficulties() {
        return Difficulty.values();
    }

    /**
     * Resolves the currently authenticated user's full {@link UserProfile} and
     * exposes it as {@code currentUser} in every template.
     * <p>
     * Returns {@code null} for anonymous (unauthenticated) visitors so templates
     * can safely use {@code th:if="${currentUser != null}"} guards.
     * </p>
     */
    @ModelAttribute("currentUser")
    public UserProfile currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Return null for unauthenticated / anonymous visitors
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userProfileService.findByUsername(auth.getName()).orElse(null);
    }

    /**
     * Exposes a boolean flag {@code isAdmin} to every template so that admin-only
     * UI elements (e.g. moderation buttons) can be conditionally rendered without
     * repeating the authority check in each controller.
     */
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
