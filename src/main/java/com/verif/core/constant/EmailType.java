package com.verif.core.constant;

/**
 * Enumeration specifying categories of email notifications dispatched by Verif.
 */
public enum EmailType {

    /**
     * Sent to user immediately upon successful profile submission,
     * communicating the "Verification Pending" state and providing tracking ID.
     */
    REGISTRATION_PENDING("Verification Request Received", "USER",
                          "Registration Acknowledged",
                          "rgba(245, 158, 11, 0.12)", "rgba(245, 158, 11, 0.3)", "#FBBF24"),

    /**
     * Sent to admin when a new user verification submission is received.
     */
    ADMIN_ALERT("Admin Alert: New Profile Submission", "ADMIN",
                "Admin Submission Alert",
                "rgba(99, 102, 241, 0.12)", "rgba(99, 102, 241, 0.3)", "#A5B4FC"),

    /**
     * Sent to user when the administrator approves their profile.
     */
    PROFILE_VERIFIED("Profile Verification Approved", "USER",
                     "Verification Approved",
                     "rgba(16, 185, 129, 0.12)", "rgba(16, 185, 129, 0.3)", "#34D399"),

    /**
     * Sent to user when the administrator cancels/rejects their profile with a reason.
     */
    PROFILE_REJECTED("Profile Verification Cancelled", "USER",
                     "Verification Cancelled",
                     "rgba(244, 63, 94, 0.12)", "rgba(244, 63, 94, 0.3)", "#FB7185");

    // Standard subject line prefix
    private final String defaultSubject;

    // Target audience role (USER or ADMIN)
    private final String targetAudience;

    // Human-readable short label for Admin dashboard outbox rendering
    private final String displayLabel;

    // Soft background color token for outbox badge chips
    private final String softBgColor;

    // Border color token for outbox badge chips
    private final String borderColor;

    // Accent text color token for outbox badge chips
    private final String accentTextColor;

    /**
     * Constructor for EmailType enum.
     * 
     * @param defaultSubject Default subject text for email dispatch
     * @param targetAudience Recipient role (USER or ADMIN)
     * @param displayLabel   Short human-friendly label for UI display
     * @param softBgColor    CSS background color for badge styling
     * @param borderColor    CSS border color for badge styling
     * @param accentTextColor CSS text color for badge styling
     */
    EmailType(String defaultSubject, String targetAudience,
              String displayLabel, String softBgColor,
              String borderColor, String accentTextColor) {
        this.defaultSubject = defaultSubject;
        this.targetAudience = targetAudience;
        this.displayLabel = displayLabel;
        this.softBgColor = softBgColor;
        this.borderColor = borderColor;
        this.accentTextColor = accentTextColor;
    }

    /**
     * Returns the default subject line.
     * 
     * @return email subject text
     */
    public String getDefaultSubject() {
        return defaultSubject;
    }

    /**
     * Returns target recipient role.
     * 
     * @return target role string
     */
    public String getTargetAudience() {
        return targetAudience;
    }

    /**
     * Returns a short, human-friendly label suitable for dashboard outbox display.
     * 
     * @return category label
     */
    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * Returns the CSS soft background color string for the outbox category chip.
     * 
     * @return CSS rgba color string
     */
    public String getSoftBgColor() {
        return softBgColor;
    }

    /**
     * Returns the CSS border color string for the outbox category chip.
     * 
     * @return CSS rgba color string
     */
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * Returns the CSS accent text color string for the outbox category chip.
     * 
     * @return CSS color string
     */
    public String getAccentTextColor() {
        return accentTextColor;
    }
}
