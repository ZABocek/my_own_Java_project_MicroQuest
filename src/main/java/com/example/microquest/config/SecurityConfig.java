package com.example.microquest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * Central Spring Security configuration for MicroQuest.
 * <p>
 * Defines the password encoder, authentication manager, HTTP security filter
 * chain (URL authorization rules, form login, logout, session management), and
 * a custom authentication failure handler that forwards the failure reason as a
 * query parameter so the login page can display a tailored error message.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructs the security configuration with the application's custom
     * {@link UserDetailsServiceImpl} injected by Spring.
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers BCrypt as the application-wide password hashing algorithm.
     * A work factor (cost) of 12 is used — strong enough for production while
     * remaining fast enough for interactive logins.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Builds the {@link AuthenticationManager} that Spring Security uses during
     * form login. It wires together our custom {@code UserDetailsService} and the
     * BCrypt password encoder so credentials are verified against hashed values
     * stored in the database.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    /**
     * Configures the entire HTTP security filter chain:
     * <ul>
     *   <li>Static assets ({@code /css/**}, {@code /js/**}) and browseable public
     *       pages (home, quests, leaderboard, user profiles) are openly accessible.</li>
     *   <li>All {@code /auth/**} endpoints (login, register, verify) are public.</li>
     *   <li>{@code /admin/**} is restricted to users holding {@code ROLE_ADMIN}.</li>
     *   <li>Every other request requires the user to be authenticated.</li>
     * </ul>
     * Form login is configured with a custom login page, and on successful login
     * users are always redirected to the home page. Sessions are invalidated on
     * logout and the JSESSIONID cookie is deleted. A maximum of 3 concurrent
     * sessions is allowed per user.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources and public pages
                .requestMatchers("/css/**", "/js/**").permitAll()
                .requestMatchers("/", "/about", "/quests", "/quests/{id}",
                                 "/leaderboard", "/users", "/users/{id}").permitAll()
                .requestMatchers("/auth/**").permitAll()
                // Admin-only
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                // Secure file download (photo IDs)
                .requestMatchers("/admin/photo-id/**").hasAuthority("ROLE_ADMIN")
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)   // destroy the server-side session
                .deleteCookies("JSESSIONID")   // remove the session cookie from the browser
                .permitAll()
            )
            .sessionManagement(session -> session
                .invalidSessionUrl("/auth/login?expired")  // redirect when session expires
                .maximumSessions(3)                        // cap concurrent sessions per user
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/auth/access-denied")   // shown when a non-admin hits /admin/**
            );

        return http.build();
    }

    /**
     * Returns a failure handler that redirects to {@code /auth/login?error} on
     * bad credentials or a locked/disabled account.
     * <p>
     * Using a redirect (not a forward) keeps the URL clean and avoids leaking
     * internal exception details to the browser.
     * </p>
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        // Passes exception class name as a query param so the login page can show tailored messages
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl("/auth/login?error");
        handler.setUseForward(false);
        return handler;
    }
}
