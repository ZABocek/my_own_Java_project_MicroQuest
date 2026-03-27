package com.example.microquest.model;

/**
 * Lifecycle states of a ban {@link Appeal}.
 * <ul>
 *   <li>{@code PENDING}  — submitted but not yet reviewed by an admin.</li>
 *   <li>{@code ACCEPTED} — admin accepted the appeal; the ban should be lifted.</li>
 *   <li>{@code REJECTED} — admin rejected the appeal; the ban stands.</li>
 * </ul>
 */
public enum AppealStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
