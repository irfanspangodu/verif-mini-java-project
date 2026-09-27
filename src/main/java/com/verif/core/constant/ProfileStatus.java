package com.verif.core.constant;

/**
 * Enumeration representing the lifecycle states of an identity verification request.
 * 
 * Flow:
 * 1. PENDING: Default state upon successful user profile submission.
 * 2. VERIFIED: Approved by an authorized administrator after review.
 * 3. REJECTED: Denied or cancelled by an administrator with an explicit reason.
 */
public enum ProfileStatus {

    /**
     * Initial status when a user submits their profile.
     * Indicates that the identity documents are awaiting administrator inspection.
     */
    PENDING("Pending Verification", "badge-pending", "#F59E0B"),

    /**
     * Positive terminal status indicating that the profile has been authenticated.
     */
    VERIFIED("Verified", "badge-verified", "#10B981"),

    /**
     * Negative terminal status indicating that the profile was rejected or cancelled.
     */
    REJECTED("Verification Cancelled", "badge-rejected", "#F43F5E");

    // Human-readable display label for the UI
    private final String displayName;

    // CSS class identifier for Bento badge styling
    private final String badgeClass;

    // Associated hex accent color for status glow indicators
    private final String accentColor;

    /**
     * Constructor for ProfileStatus enum constants.
     * 
     * @param displayName Human-readable label for display in JSF views
     * @param badgeClass  CSS class name applied to badge tags
     * @param accentColor Hex color code representing the status
     */
    ProfileStatus(String displayName, String badgeClass, String accentColor) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.accentColor = accentColor;
    }

    /**
     * Retrieves the friendly display name.
     * 
     * @return display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retrieves the CSS badge style class name.
     * 
     * @return CSS class name
     */
    public String getBadgeClass() {
        return badgeClass;
    }

    /**
     * Retrieves the visual hex color associated with this state.
     * 
     * @return hex color code string
     */
    public String getAccentColor() {
        return accentColor;
    }
}
