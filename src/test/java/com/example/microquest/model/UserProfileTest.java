package com.example.microquest.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    @Test
    void prePersist_setsCreatedAt() {
        UserProfile user = new UserProfile();

        user.prePersist();

        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void settersAndGetters_workCorrectly() {
        UserProfile user = new UserProfile();
        user.setId(7L);
        user.setUsername("alice");
        user.setDisplayName("Alice Wonder");
        user.setHomeCity("New York");
        user.setBio("Loves adventures.");

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getDisplayName()).isEqualTo("Alice Wonder");
        assertThat(user.getHomeCity()).isEqualTo("New York");
        assertThat(user.getBio()).isEqualTo("Loves adventures.");
    }

    @Test
    void optionalFields_defaultToNull() {
        UserProfile user = new UserProfile();

        assertThat(user.getHomeCity()).isNull();
        assertThat(user.getBio()).isNull();
    }

    @Test
    void collections_defaultToEmptyLists() {
        UserProfile user = new UserProfile();

        assertThat(user.getAuthoredQuests()).isNotNull().isEmpty();
        assertThat(user.getComments()).isNotNull().isEmpty();
        assertThat(user.getSavedQuests()).isNotNull().isEmpty();
    }
}
