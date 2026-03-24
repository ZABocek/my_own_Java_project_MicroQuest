package com.example.microquest.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestTest {

    @Test
    void prePersist_setsCreatedAtAndUpdatedAt() {
        Quest quest = new Quest();

        quest.prePersist();

        assertThat(quest.getCreatedAt()).isNotNull();
        assertThat(quest.getUpdatedAt()).isNotNull();
    }

    @Test
    void prePersist_setsCreatedAtAndUpdatedAtToSameInstant() {
        Quest quest = new Quest();

        quest.prePersist();

        // Both timestamps are set to the same LocalDateTime.now() in the same call
        assertThat(quest.getCreatedAt()).isEqualTo(quest.getUpdatedAt());
    }

    @Test
    void preUpdate_setsUpdatedAt() {
        Quest quest = new Quest();
        quest.prePersist();

        quest.preUpdate();

        assertThat(quest.getUpdatedAt()).isNotNull();
    }

    @Test
    void settersAndGetters_workCorrectly() {
        Quest quest = new Quest();
        quest.setId(42L);
        quest.setTitle("Test Quest");
        quest.setSummary("A summary");
        quest.setDescription("A description");
        quest.setCategory(Category.OUTDOORS);
        quest.setDifficulty(Difficulty.HARD);
        quest.setEstimatedMinutes(90);
        quest.setIndoor(true);
        quest.setTags("tag1, tag2");

        assertThat(quest.getId()).isEqualTo(42L);
        assertThat(quest.getTitle()).isEqualTo("Test Quest");
        assertThat(quest.getSummary()).isEqualTo("A summary");
        assertThat(quest.getDescription()).isEqualTo("A description");
        assertThat(quest.getCategory()).isEqualTo(Category.OUTDOORS);
        assertThat(quest.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(quest.getEstimatedMinutes()).isEqualTo(90);
        assertThat(quest.isIndoor()).isTrue();
        assertThat(quest.getTags()).isEqualTo("tag1, tag2");
    }

    @Test
    void commentsAndSaves_defaultToEmptyLists() {
        Quest quest = new Quest();

        assertThat(quest.getComments()).isNotNull().isEmpty();
        assertThat(quest.getSaves()).isNotNull().isEmpty();
    }
}
