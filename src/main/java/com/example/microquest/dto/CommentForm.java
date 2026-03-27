package com.example.microquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form DTO for adding a comment to a quest.
 * Body is limited to 800 characters and must not be blank.
 */
public class CommentForm {

    @NotBlank
    @Size(max = 800)
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
