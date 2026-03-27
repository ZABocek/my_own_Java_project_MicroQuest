package com.example.microquest.config;

import com.example.microquest.model.Role;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.UserProfileRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bridges Spring Security's authentication pipeline with the application's
 * database-backed {@link UserProfile} entity.
 * <p>
 * Spring Security calls {@link #loadUserByUsername(String)} during every login
 * attempt. This implementation normalises the username (trim + lower-case),
 * looks it up in the database, and converts the result into a Spring
 * {@link UserDetails} object.  The account-locked flag reflects both the
 * {@code active} column (permanently banned / deleted accounts) and the
 * {@code bannedUntil} timestamp (temporary bans).
 * </p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserProfileRepository userProfileRepository;

    /** Constructor-injected repository used to load users from the database. */
    public UserDetailsServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Loads a {@link UserDetails} object for the given username.
     * <p>
     * The username is normalised (trimmed and lower-cased) before the database
     * look-up so that logins are case-insensitive.  An account is considered
     * <em>locked</em> (preventing login) if:
     * <ul>
     *   <li>the {@code active} flag is {@code false} (permanent ban), or</li>
     *   <li>the {@code bannedUntil} timestamp is still in the future (temp ban).</li>
     * </ul>
     * </p>
     *
     * @param username the username submitted in the login form
     * @return a populated {@link UserDetails} ready for Spring Security's
     *         credential-checking step
     * @throws UsernameNotFoundException if no matching account exists or the
     *                                   account has no password hash set
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Normalise so logins are case-insensitive (all usernames are stored lower-case)
        String normalized = username.trim().toLowerCase();
        UserProfile profile = userProfileRepository.findByUsername(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for: " + username));

        // Guard against accounts that were created without a password (e.g. partial seed data)
        if (profile.getPasswordHash() == null || profile.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("Account has no password set");
        }

        // An account is locked if it is deactivated OR its temporary ban has not yet expired
        boolean accountNonLocked = profile.isActive()
                && (profile.getBannedUntil() == null || profile.getBannedUntil().isBefore(LocalDateTime.now()));

        return User.builder()
                .username(profile.getUsername())
                .password(profile.getPasswordHash())
                // Map the application Role enum to a Spring GrantedAuthority string
                .authorities(List.of(new SimpleGrantedAuthority(
                        profile.getRole() != null ? profile.getRole().name() : Role.ROLE_USER.name())))
                .disabled(!profile.isActive())       // permanently banned/deleted accounts are disabled
                .accountLocked(!accountNonLocked)    // temporarily banned accounts are locked
                .build();
    }
}
