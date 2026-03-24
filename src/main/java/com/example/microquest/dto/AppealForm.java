package com.example.microquest.dto;

import jakarta.validation.constraints.*;

public class AppealForm {

    @NotNull
    private Long banRecordId;

    @NotBlank(message = "Please write your appeal message")
    @Size(min = 10, max = 2000, message = "Appeal message must be between 10 and 2000 characters")
    private String message;

    public Long getBanRecordId() { return banRecordId; }
    public void setBanRecordId(Long banRecordId) { this.banRecordId = banRecordId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
