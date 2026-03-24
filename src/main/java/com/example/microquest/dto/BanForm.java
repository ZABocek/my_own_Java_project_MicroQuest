package com.example.microquest.dto;

import jakarta.validation.constraints.*;

public class BanForm {

    @NotNull
    private Long userId;

    @NotBlank(message = "A reason is required")
    @Size(max = 1000)
    private String reason;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
