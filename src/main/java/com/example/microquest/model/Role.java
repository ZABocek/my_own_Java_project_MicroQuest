package com.example.microquest.model;

/**
 * Spring Security role assigned to a {@link UserProfile}.
 * <ul>
 *   <li>{@code ROLE_USER}  — standard registered user.</li>
 *   <li>{@code ROLE_ADMIN} — platform administrator with full moderation access.</li>
 * </ul>
 * The {@code ROLE_} prefix is the Spring Security convention for authority names.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
