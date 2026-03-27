package com.example.microquest.repository;

/**
 * JPQL projection interface returned by
 * {@link UserProfileRepository#findTopCreators}.
 * Each row represents one user with their total authored-quest count.
 */
public interface UserLeaderboardRow {
    /** Database ID of the user. */
    Long getUserId();
    /** Display name of the user shown on the leaderboard. */
    String getDisplayName();
    /** Total number of quests authored by this user. */
    Long getQuestCount();
}
