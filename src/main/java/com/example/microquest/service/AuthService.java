package com.example.microquest.service;

import com.example.microquest.dto.PasswordChangeForm;
import com.example.microquest.dto.RegistrationForm;
import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    public AuthService(UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService,
                       EmailService emailService) {
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    public UserProfile register(RegistrationForm form, MultipartFile photoIdFile) {
        String username = form.getUsername().trim().toLowerCase();
        String email = form.getEmail().trim().toLowerCase();

        if (userProfileRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userProfileRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email address is already registered");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String verificationToken = UUID.randomUUID().toString();

        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setDisplayName(form.getDisplayName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(verificationToken);
        if (form.getHomeCity() != null && !form.getHomeCity().isBlank()) {
            user.setHomeCity(form.getHomeCity().trim());
        }
        if (form.getBio() != null && !form.getBio().isBlank()) {
            user.setBio(form.getBio().trim());
        }

        UserProfile saved = userProfileRepository.save(user);

        // Store photo ID after we have the user ID
        if (photoIdFile != null && !photoIdFile.isEmpty()) {
            try {
                String path = fileStorageService.storePhotoId(photoIdFile, saved.getId());
                saved.setPhotoIdPath(path);
                saved = userProfileRepository.save(saved);
            } catch (IOException | IllegalArgumentException e) {
                log.warn("Could not store photo ID for user {}: {}", saved.getId(), e.getMessage());
            }
        }

        // Send verification email (best-effort)
        try {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getDisplayName(), verificationToken);
        } catch (Exception e) {
            log.warn("Could not send welcome email to {}: {}", saved.getEmail(), e.getMessage());
        }

        return saved;
    }

    public boolean verifyEmail(String token) {
        return userProfileRepository.findByEmailVerificationToken(token)
                .map(user -> {
                    user.setEmailVerified(true);
                    user.setEmailVerificationToken(null);
                    userProfileRepository.save(user);
                    return true;
                }).orElse(false);
    }

    public void changePassword(UserProfile user, PasswordChangeForm form) {
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userProfileRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfile findByUsername(String username) {
        return userProfileRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
