package com.example.microquest.dto;

import jakarta.validation.constraints.*;

/**
 * Form DTO for the optional text caption that accompanies a GIF submission.
 * The actual file upload is handled separately via {@code @RequestParam("gif")}.
 */
public class SubmissionForm {

    @Size(max = 500, message = "Caption may not exceed 500 characters")
    private String caption;

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
