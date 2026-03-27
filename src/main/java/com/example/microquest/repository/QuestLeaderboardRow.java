package com.example.microquest.repository;

/**
 * JPQL projection interface returned by
 * {@link QuestRepository#findTopSavedQuests}.
 * Each row represents one approved quest with its save-count aggregation.
 */
public interface QuestLeaderboardRow {
    /** Database ID of the quest. */
    Long getQuestId();
    /** Title of the quest. */
    String getTitle();
    /** Number of times the quest has been saved by users. */
    Long getSaveCount();
}
