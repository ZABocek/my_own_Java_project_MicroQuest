package com.example.microquest.controller;

import com.example.microquest.model.UserProfile;
import com.example.microquest.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    // Covers both UserController and ViewModelAdvice
    @MockitoBean UserProfileService userProfileService;

    // ── GET /users ────────────────────────────────────────────────────────────

    @Test
    void listUsers_returns200andListView() throws Exception {
        when(userProfileService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void listUsers_populatesUsersModelAttribute() throws Exception {
        UserProfile user = userProfile(1L, "alice", "Alice");
        when(userProfileService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", List.of(user)));
    }

    // ── GET /users/new ────────────────────────────────────────────────────────

    @Test
    void showCreateUserForm_returns200andFormView() throws Exception {
        mockMvc.perform(get("/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/form"))
                .andExpect(model().attributeExists("userProfileForm"));
    }

    // ── POST /users ───────────────────────────────────────────────────────────

    @Test
    void createUser_validForm_redirectsToUserDetail() throws Exception {
        UserProfile created = userProfile(5L, "bob", "Bob");
        when(userProfileService.createUser(any())).thenReturn(created);

        mockMvc.perform(post("/users")
                        .param("username", "bob")
                        .param("displayName", "Bob"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/users/*"));
    }

    @Test
    void createUser_blankUsername_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/users")
                        .param("username", "")
                        .param("displayName", "Bob"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/form"))
                .andExpect(model().attributeHasFieldErrors("userProfileForm", "username"));
    }

    @Test
    void createUser_blankDisplayName_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/users")
                        .param("username", "bob")
                        .param("displayName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("users/form"))
                .andExpect(model().attributeHasFieldErrors("userProfileForm", "displayName"));
    }

    // ── GET /users/{id} ───────────────────────────────────────────────────────

    @Test
    void showUser_existingId_returns200andDetailView() throws Exception {
        UserProfile user = userProfile(1L, "alice", "Alice");
        when(userProfileService.getUserOrThrow(1L)).thenReturn(user);
        when(userProfileService.getAuthoredQuests(1L)).thenReturn(Collections.emptyList());
        when(userProfileService.getSavedQuests(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/detail"))
                .andExpect(model().attribute("user", user));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static UserProfile userProfile(Long id, String username, String displayName) {
        UserProfile u = new UserProfile();
        u.setId(id);
        u.setUsername(username);
        u.setDisplayName(displayName);
        return u;
    }
}
