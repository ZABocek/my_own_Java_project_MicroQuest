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

@ControllerAdvice
public class ViewModelAdvice {

    private final UserProfileService userProfileService;

    public ViewModelAdvice(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    @ModelAttribute("difficulties")
    public Difficulty[] difficulties() {
        return Difficulty.values();
    }

    @ModelAttribute("currentUser")
    public UserProfile currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userProfileService.findByUsername(auth.getName()).orElse(null);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
