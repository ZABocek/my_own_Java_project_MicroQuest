package com.example.microquest.repository;

import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for {@link com.example.microquest.model.UserProfile} entities.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** Looks up a user by (case-sensitive) username; used by Spring Security and controllers. */
    Optional<UserProfile> findByUsername(String username);

    /** Finds a user by email address; used during registration to check for duplicates. */
    Optional<UserProfile> findByEmail(String email);

    /** Finds a user by their email-verification token; used in the email-verify flow. */
    Optional<UserProfile> findByEmailVerificationToken(String token);

    /** Returns all users sorted by display name (for the admin user list). */
    List<UserProfile> findAllByOrderByDisplayNameAsc();

    /** Returns all users with a specific role, sorted by display name. */
    List<UserProfile> findAllByRoleOrderByDisplayNameAsc(Role role);

    /**
     * Ranks users by the number of quests they have authored, most prolific first.
     * Ties are broken alphabetically by display name.
     * The {@code Pageable} argument limits results (e.g. top 10 for the leaderboard).
     */
    @Query("""
            select u.id as userId, u.displayName as displayName, count(q.id) as questCount
            from UserProfile u
            left join u.authoredQuests q
            group by u.id, u.displayName
            order by count(q.id) desc, u.displayName asc
            """)
    List<UserLeaderboardRow> findTopCreators(Pageable pageable);
}

