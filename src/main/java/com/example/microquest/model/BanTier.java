package com.example.microquest.model;

/**
 * Escalating ban-duration tiers.
 * <ul>
 *   <li>{@code ONE_MONTH}    — first offence: 30-day temporary ban.</li>
 *   <li>{@code THREE_MONTHS} — second offence: 90-day temporary ban.</li>
 *   <li>{@code PERMANENT}    — third+ offence: account deactivated.</li>
 * </ul>
 * The tier is selected automatically in
 * {@link com.example.microquest.service.BanService#issueBan} based on
 * the user's {@code banCount}.
 */
public enum BanTier {
    ONE_MONTH,
    THREE_MONTHS,
    PERMANENT
}
