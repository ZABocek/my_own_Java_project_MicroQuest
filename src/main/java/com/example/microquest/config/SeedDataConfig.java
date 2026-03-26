package com.example.microquest.config;

import com.example.microquest.model.Category;
import com.example.microquest.model.Comment;
import com.example.microquest.model.Difficulty;
import com.example.microquest.model.Quest;
import com.example.microquest.model.QuestSave;
import com.example.microquest.model.QuestStatus;
import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.CommentRepository;
import com.example.microquest.repository.QuestRepository;
import com.example.microquest.repository.QuestSaveRepository;
import com.example.microquest.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    /** Demo password for non-admin seed users. */
    private static final String DEMO_PASSWORD = "Demo123!";

    @Bean
    CommandLineRunner seedData(UserProfileRepository userProfileRepository,
                               QuestRepository questRepository,
                               CommentRepository commentRepository,
                               QuestSaveRepository questSaveRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${admin.username:drewadoo}") String adminUsername,
                               @Value("${admin.email:zabocek@gmail.com}") String adminEmail,
                               @Value("${admin.password:Eur0p3!$}") String adminPassword,
                               @Value("${admin.displayName:Drewadoo}") String adminDisplayName) {
        return args -> {
            if (userProfileRepository.count() > 0 || questRepository.count() > 0) {
                // Ensure admin account always exists even after DB already seeded
                ensureAdminExists(userProfileRepository, passwordEncoder,
                        adminUsername, adminEmail, adminPassword, adminDisplayName);
                return;
            }

            log.info("Seeding initial data...");

            // â”€â”€ Admin user (Drewadoo) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            UserProfile drewadoo = buildUser(adminUsername.toLowerCase(), adminDisplayName,
                    adminEmail.toLowerCase(), passwordEncoder.encode(adminPassword),
                    Role.ROLE_ADMIN, "Chicago",
                    "Site administrator and quest curator.");
            userProfileRepository.save(drewadoo);

            // â”€â”€ Regular seed users â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            String demoHash = passwordEncoder.encode(DEMO_PASSWORD);

            UserProfile maya = buildUser("maya", "Maya Carter",
                    "maya@example.com", demoHash, Role.ROLE_USER, "Chicago",
                    "Collects unusual cafe moments and tiny city adventures.");
            userProfileRepository.save(maya);

            UserProfile leo = buildUser("leo", "Leo Tanaka",
                    "leo@example.com", demoHash, Role.ROLE_USER, "Seattle",
                    "Likes fitness quests, street photography, and interesting bookstores.");
            userProfileRepository.save(leo);

            UserProfile nina = buildUser("nina", "Nina Patel",
                    "nina@example.com", demoHash, Role.ROLE_USER, "Boston",
                    "Builds relaxing quests that help people slow down and recharge.");
            userProfileRepository.save(nina);

            // â”€â”€ Quests â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Quest q1 = buildQuest("Find the coziest independent coffee corner",
                    "Visit a local cafe, order something new, and rate the vibe.",
                    "Pick one independent coffee spot you have never tried before. Sit for at least twenty minutes, notice the music, the smell, and the seating, then write down three details you liked.",
                    Category.CITY_EXPLORATION, Difficulty.EASY, 35, true, "coffee,city,relaxing", maya);

            Quest q2 = buildQuest("Create a five-photo color challenge",
                    "Take five photos in one color family while walking through your neighborhood.",
                    "Choose a color like blue, red, or green. During a short walk, photograph five different objects that fit the theme. Bonus points if one shot includes something surprising.",
                    Category.CREATIVE, Difficulty.EASY, 25, false, "photo,creative,walking", leo);

            Quest q3 = buildQuest("Design a zero-cost reset evening",
                    "Plan a calm, free evening that actually improves your mood.",
                    "Build a one-hour evening reset using only free things: tea, stretching, a short journal entry, music, or a walk. Share the order that worked best for you.",
                    Category.RELAXATION, Difficulty.MEDIUM, 60, true, "wellness,evening,reset", nina);

            Quest q4 = buildQuest("Create a gif of a memorable event",
                    "Capture a moment from your day as an animated GIF and share it with a caption.",
                    "Use a phone app, screen recorder, or any tool you like to create a short animated GIF of something memorable from your day — a funny moment, a beautiful scene, or anything that made you smile. Upload it here with a caption that tells the story.",
                    Category.CREATIVE, Difficulty.EASY, 20, false, "gif,creative,memory,social", drewadoo);

            questRepository.save(q1);
            questRepository.save(q2);
            questRepository.save(q3);
            questRepository.save(q4);

            // â”€â”€ Comments â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Comment c1 = new Comment();
            c1.setQuest(q1);
            c1.setAuthor(leo);
            c1.setBody("This one is great because it works in almost any city.");

            Comment c2 = new Comment();
            c2.setQuest(q2);
            c2.setAuthor(nina);
            c2.setBody("I love how easy this is to complete on a lunch break.");

            commentRepository.save(c1);
            commentRepository.save(c2);

            // â”€â”€ Quest saves â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            QuestSave s1 = new QuestSave(); s1.setQuest(q1); s1.setUser(nina);
            QuestSave s2 = new QuestSave(); s2.setQuest(q2); s2.setUser(maya);
            QuestSave s3 = new QuestSave(); s3.setQuest(q1); s3.setUser(leo);
            questSaveRepository.save(s1);
            questSaveRepository.save(s2);
            questSaveRepository.save(s3);

            log.info("Seed data complete. Admin login: {} / {}", adminUsername, adminPassword);
            log.info("Demo user password: {}", DEMO_PASSWORD);
        };
    }

    private void ensureAdminExists(UserProfileRepository repo, PasswordEncoder encoder,
                                   String username, String email, String rawPassword, String displayName) {
        String normalizedUsername = username.toLowerCase();
        if (repo.findByUsername(normalizedUsername).isEmpty()) {
            log.info("Creating missing admin account: {}", normalizedUsername);
            UserProfile admin = buildUser(normalizedUsername, displayName, email.toLowerCase(),
                    encoder.encode(rawPassword), Role.ROLE_ADMIN, null, "Site administrator.");
            repo.save(admin);
        }
    }

    private static UserProfile buildUser(String username, String displayName, String email,
                                          String passwordHash, Role role, String homeCity, String bio) {
        UserProfile u = new UserProfile();
        u.setUsername(username);
        u.setDisplayName(displayName);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        u.setActive(true);
        u.setEmailVerified(true); // Seed users pre-verified
        u.setHomeCity(homeCity);
        u.setBio(bio);
        return u;
    }

    private static Quest buildQuest(String title, String summary, String description,
                                     Category category, Difficulty difficulty,
                                     int minutes, boolean indoor, String tags, UserProfile author) {
        Quest q = new Quest();
        q.setTitle(title);
        q.setSummary(summary);
        q.setDescription(description);
        q.setCategory(category);
        q.setDifficulty(difficulty);
        q.setEstimatedMinutes(minutes);
        q.setIndoor(indoor);
        q.setTags(tags);
        q.setAuthor(author);
        q.setStatus(QuestStatus.APPROVED); // Seed quests are auto-approved
        return q;
    }
}
