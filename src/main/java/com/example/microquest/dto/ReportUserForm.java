package com.example.microquest.dto;

import jakarta.validation.constraints.*;

public class ReportUserForm {

    @NotNull
    private Long reportedUserId;

    @NotBlank(message = "Please describe the issue")
    @Size(min = 10, max = 1000, message = "Report reason must be between 10 and 1000 characters")
    private String reason;

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
