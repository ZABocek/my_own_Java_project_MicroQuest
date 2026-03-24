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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserProfileRepository userProfileRepository;

    public UserDetailsServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username.trim().toLowerCase();
        UserProfile profile = userProfileRepository.findByUsername(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for: " + username));

        if (profile.getPasswordHash() == null || profile.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("Account has no password set");
        }

        boolean accountNonLocked = profile.isActive()
                && (profile.getBannedUntil() == null || profile.getBannedUntil().isBefore(LocalDateTime.now()));

        return User.builder()
                .username(profile.getUsername())
                .password(profile.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(
                        profile.getRole() != null ? profile.getRole().name() : Role.ROLE_USER.name())))
                .disabled(!profile.isActive())
                .accountLocked(!accountNonLocked)
                .build();
    }
}
