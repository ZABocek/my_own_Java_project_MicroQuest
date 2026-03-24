package com.example.microquest.repository;

import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findByEmail(String email);

    Optional<UserProfile> findByEmailVerificationToken(String token);

    List<UserProfile> findAllByOrderByDisplayNameAsc();

    List<UserProfile> findAllByRoleOrderByDisplayNameAsc(Role role);

    @Query("""
            select u.id as userId, u.displayName as displayName, count(q.id) as questCount
            from UserProfile u
            left join u.authoredQuests q
            group by u.id, u.displayName
            order by count(q.id) desc, u.displayName asc
            """)
    List<UserLeaderboardRow> findTopCreators(Pageable pageable);
}

