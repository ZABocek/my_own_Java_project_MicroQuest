package com.example.microquest.model;

/**
 * Approval workflow state for a {@link Quest}.
 * <ul>
 *   <li>{@code PENDING_APPROVAL} — default state after creation by a non-admin.</li>
 *   <li>{@code APPROVED}          — visible to all users on the quest list.</li>
 *   <li>{@code REJECTED}          — hidden; admin provided a rejection reason.</li>
 * </ul>
 */
public enum QuestStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}
