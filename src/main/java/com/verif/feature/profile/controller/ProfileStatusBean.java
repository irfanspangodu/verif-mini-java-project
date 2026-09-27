package com.verif.feature.profile.controller;

import com.verif.core.constant.ProfileStatus;
import com.verif.core.util.DateTimeUtils;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.service.ProfileService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * JSF Managed Bean powering the real-time profile status verification tracker (`status.xhtml`).
 * 
 * Facilitates:
 * - Direct tracking code lookups.
 * - Automatic status polling without full-page reloads.
 * - "Verification Pending" banner display on initial entry.
 * - Interactive Bento timeline progression.
 */
@Named("profileStatusBean")
@ViewScoped
public class ProfileStatusBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ProfileStatusBean.class);

    // Injected service for profile queries
    private final ProfileService profileService;

    // Tracking code input or query parameter value
    private String searchCode;

    // Loaded profile model
    private UserProfile currentProfile;

    // Flag indicating user arrived right after new submission
    private boolean newSubmission = false;

    // Flag indicating search attempt occurred
    private boolean searched = false;

    // Last status refresh timestamp for display
    private String lastUpdatedTime;

    /**
     * Constructor injection of ProfileService.
     * 
     * @param profileService Injected ProfileService
     */
    @Autowired
    public ProfileStatusBean(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Post-construct initialization hook.
     * Reads HTTP GET query parameters (trackingCode, newSubmission).
     */
    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        String codeParam = params.get("trackingCode");
        String newSubParam = params.get("newSubmission");

        if ("true".equalsIgnoreCase(newSubParam)) {
            this.newSubmission = true;
        }

        if (codeParam != null && !codeParam.trim().isEmpty()) {
            this.searchCode = codeParam.trim().toUpperCase();
            lookupProfile();
        }

        this.lastUpdatedTime = DateTimeUtils.nowFriendly();
    }

    /**
     * Action method to look up profile by tracking code.
     */
    public void lookupProfile() {
        this.searched = true;
        if (searchCode == null || searchCode.trim().isEmpty()) {
            this.currentProfile = null;
            return;
        }

        Optional<UserProfile> opt = profileService.findByTrackingCode(searchCode.trim().toUpperCase());
        this.currentProfile = opt.orElse(null);
        this.lastUpdatedTime = DateTimeUtils.nowFriendly();

        if (this.currentProfile != null) {
            logger.info("Found profile for tracking code: {} with status: {}", 
                        searchCode, currentProfile.getVerificationStatus());
        } else {
            logger.info("No profile found for tracking code: {}", searchCode);
        }
    }

    /**
     * Resets the lookup form and clears any previously resolved profile.
     */
    public void clearLookup() {
        this.searchCode = null;
        this.currentProfile = null;
        this.searched = false;
        this.newSubmission = false;
    }

    /**
     * Action method triggered via Ajax poll to check for real-time status transitions.
     * Updates profile state without altering user position or disrupting view.
     */
    public void pollStatus() {
        if (this.currentProfile != null) {
            Optional<UserProfile> refreshed = profileService.findById(this.currentProfile.getId());
            refreshed.ifPresent(p -> this.currentProfile = p);
            this.lastUpdatedTime = DateTimeUtils.nowFriendly();
        }
    }

    /**
     * Determines the active step in the Bento verification progression.
     * Step 1 "Application Received" is always complete once a profile exists
     * (the view marks it completed unconditionally), so this method reports
     * the two states that actually change over time:
     * 2: Under Review (PENDING)
     * 3: Finalized (VERIFIED or REJECTED)
     * 
     * @return Integer step level (0 when no profile is loaded, 2, or 3)
     */
    public int getTimelineStep() {
        if (currentProfile == null) {
            return 0;
        }
        if (currentProfile.getVerificationStatus() == ProfileStatus.PENDING) {
            return 2;
        }
        return 3;
    }

    /**
     * Indicates whether the background status poll should be active.
     * Polling before a profile has been resolved is pure wasted traffic, so the
     * hidden polling form in status.xhtml is rendered only when this is true.
     * 
     * @return true when a profile is loaded and could change status
     */
    public boolean isPollEnabled() {
        return currentProfile != null;
    }

    /**
     * Checks if current profile is in PENDING state.
     * 
     * @return true if pending
     */
    public boolean isPending() {
        return currentProfile != null && currentProfile.getVerificationStatus() == ProfileStatus.PENDING;
    }

    /**
     * Checks if current profile is in VERIFIED state.
     * 
     * @return true if verified
     */
    public boolean isVerified() {
        return currentProfile != null && currentProfile.getVerificationStatus() == ProfileStatus.VERIFIED;
    }

    /**
     * Checks if current profile is in REJECTED state.
     * 
     * @return true if rejected
     */
    public boolean isRejected() {
        return currentProfile != null && currentProfile.getVerificationStatus() == ProfileStatus.REJECTED;
    }

    /**
     * Returns friendly formatted submission date.
     * 
     * @return Formatted timestamp string
     */
    public String getFormattedCreatedAt() {
        return currentProfile != null ? DateTimeUtils.formatFriendly(currentProfile.getCreatedAt()) : "N/A";
    }

    /**
     * Returns friendly formatted verification finalization date.
     * 
     * @return Formatted timestamp string
     */
    public String getFormattedVerifiedAt() {
        return currentProfile != null ? DateTimeUtils.formatFriendly(currentProfile.getVerifiedAt()) : "N/A";
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public String getSearchCode() {
        return searchCode;
    }

    public void setSearchCode(String searchCode) {
        this.searchCode = searchCode;
    }

    public UserProfile getCurrentProfile() {
        return currentProfile;
    }

    public boolean isNewSubmission() {
        return newSubmission;
    }

    public void setNewSubmission(boolean newSubmission) {
        this.newSubmission = newSubmission;
    }

    public boolean isSearched() {
        return searched;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }
}
