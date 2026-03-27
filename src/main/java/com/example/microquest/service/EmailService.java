package com.example.microquest.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Best-effort HTML email service.
 * <p>
 * All public send methods delegate to the private {@link #trySend} helper which
 * catches any {@link jakarta.mail.MessagingException} and logs a warning rather
 * than propagating the exception.  This ensures that SMTP failures never abort
 * user-facing operations (registration, ban, appeal, quest approval).
 * </p>
 * <p>
 * If {@code spring.mail.username} is not configured the service skips sending
 * entirely and logs an informational message instead.
 * </p>
 * <p>
 * All user-supplied strings are passed through {@link #htmlEscape} before being
 * embedded in the HTML body to prevent XSS in email clients.
 * </p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.base-url:http://localhost:8080}") String baseUrl,
                        @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.fromAddress = fromAddress;
    }

    /** Sends an HTML welcome email with a one-time email-verification link. */
    public void sendWelcomeEmail(String toEmail, String displayName, String verificationToken) {
        String link = baseUrl + "/auth/verify?token=" + verificationToken;
        String body = "<h2>Welcome to MicroQuest, " + htmlEscape(displayName) + "!</h2>"
                + "<p>Click below to verify your email address:</p>"
                + "<p><a href='" + link + "' style='background:#0d6efd;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>Verify Email</a></p>"
                + "<p>If you did not register, please ignore this email.</p>";
        trySend(toEmail, "MicroQuest – Verify your email", body);
    }

    /** Sends the temporary-ban notification with expiry date and an appeal link. */
    public void sendTempBanEmail(String toEmail, String displayName, String reason, String expiryDate, Long banRecordId) {
        String appealLink = baseUrl + "/appeal/submit/" + banRecordId;
        String body = "<h2>MicroQuest – Account Temporarily Suspended</h2>"
                + "<p>Hello " + htmlEscape(displayName) + ",</p>"
                + "<p>Your account has been temporarily suspended.</p>"
                + "<p><strong>Reason:</strong> " + htmlEscape(reason) + "</p>"
                + "<p><strong>Suspension expires:</strong> " + expiryDate + "</p>"
                + "<p>If you believe this is an error, you may <a href='" + appealLink + "'>submit an appeal</a>.</p>";
        trySend(toEmail, "MicroQuest – Account Suspended", body);
    }

    /** Sends the permanent-ban notification. No appeal link is included. */
    public void sendPermanentBanEmail(String toEmail, String displayName, String reason) {
        String body = "<h2>MicroQuest – Account Permanently Suspended</h2>"
                + "<p>Hello " + htmlEscape(displayName) + ",</p>"
                + "<p>Your account has been permanently suspended from MicroQuest.</p>"
                + "<p><strong>Reason:</strong> " + htmlEscape(reason) + "</p>";
        trySend(toEmail, "MicroQuest – Account Permanently Suspended", body);
    }

    /** Sends the appeal-decision email indicating whether the ban was lifted. */
    public void sendAppealDecisionEmail(String toEmail, String displayName, boolean accepted, String adminResponse) {
        String result = accepted ? "Accepted" : "Rejected";
        String body = "<h2>MicroQuest – Appeal " + result + "</h2>"
                + "<p>Hello " + htmlEscape(displayName) + ",</p>"
                + "<p>Your ban appeal has been <strong>" + result.toLowerCase() + "</strong>.</p>"
                + "<p><strong>Admin response:</strong> " + htmlEscape(adminResponse) + "</p>"
                + (accepted ? "<p>Your account access has been restored.</p>" : "");
        trySend(toEmail, "MicroQuest – Appeal " + result, body);
    }

    /** Notifies the quest author that their quest was approved and is now live. */
    public void sendQuestApprovedEmail(String toEmail, String displayName, String questTitle) {
        String body = "<h2>MicroQuest – Quest Approved!</h2>"
                + "<p>Hello " + htmlEscape(displayName) + ",</p>"
                + "<p>Your quest <strong>" + htmlEscape(questTitle) + "</strong> has been approved and is now live!</p>";
        trySend(toEmail, "MicroQuest – Quest Approved", body);
    }

    /** Notifies the quest author that their quest was rejected and why. */
    public void sendQuestRejectedEmail(String toEmail, String displayName, String questTitle, String reason) {
        String body = "<h2>MicroQuest – Quest Not Approved</h2>"
                + "<p>Hello " + htmlEscape(displayName) + ",</p>"
                + "<p>Your quest <strong>" + htmlEscape(questTitle) + "</strong> was not approved.</p>"
                + "<p><strong>Reason:</strong> " + htmlEscape(reason) + "</p>"
                + "<p>You are welcome to make changes and resubmit.</p>";
        trySend(toEmail, "MicroQuest – Quest Not Approved", body);
    }

    /**
     * Sends a single HTML email; skips silently if SMTP is not configured.
     * Any {@link jakarta.mail.MessagingException} is caught and logged as a warning.
     */
    private void trySend(String to, String subject, String htmlBody) {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.info("[Email] SMTP not configured – skipped. To={} Subject={}", to, subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("[Email] Sent to={} subject={}", to, subject);
        } catch (MessagingException e) {
            log.warn("[Email] Failed to send to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Escapes HTML special characters in user-supplied strings before embedding
     * them in the email body, preventing XSS in email clients.
     */
    private String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
