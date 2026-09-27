package com.verif.feature.admin.controller;

import com.verif.feature.admin.dto.DashboardStatisticsDto;
import com.verif.feature.admin.service.AdminVerificationService;
import com.verif.feature.notification.model.EmailNotificationLog;
import com.verif.feature.notification.service.EmailService;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.service.ProfileService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

/**
 * JSF Managed Bean backing the Admin Verification Dashboard (`admin/dashboard.xhtml`).
 * 
 * Provides:
 * - Real-time Bento Grid statistics.
 * - Pending verification request queue.
 * - Inline Ajax verification approval.
 * - Modal-driven cancellation workflow with required reason capture.
 * - Live Outbox email notification audit feed.
 * - Real-time auto-refresh polling.
 */
@Named("adminDashboardBean")
@ViewScoped
public class AdminDashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardBean.class);

    // Injected service for admin operations
    private final AdminVerificationService adminService;

    // Injected service for reading recent notification outbox logs
    private final EmailService emailService;

    // Injected service providing the applicant search capability
    private final ProfileService profileService;

    // Aggregated statistics for Bento KPI cards
    private DashboardStatisticsDto statistics;

    // Pending profiles awaiting admin decision
    private List<UserProfile> pendingProfiles;

    // All profiles for history table
    private List<UserProfile> allProfiles;

    // Recent outbox notifications for live audit feed
    private List<EmailNotificationLog> recentEmails;

    // Selected profile undergoing cancellation review
    private UserProfile selectedProfileForAction;

    // Form input for cancellation/rejection reason
    private String rejectionReason;

    // Boolean flag controlling visibility of rejection modal overlay
    private boolean showRejectModal = false;

    // Tab view selection: "pending", "all", "emails"
    private String activeTab = "pending";

    // Free-text query used to filter the applicant registry
    private String searchQuery;

    // Flag indicating that the registry currently shows filtered results
    private boolean searchActive = false;

    /**
     * Constructor injection of admin, email and profile services.
     */
    @Autowired
    public AdminDashboardBean(AdminVerificationService adminService,
                              EmailService emailService,
                              ProfileService profileService) {
        this.adminService = adminService;
        this.emailService = emailService;
        this.profileService = profileService;
    }

    /**
     * Post-construct initialization hook.
     * Loads initial metrics, pending requests, and email logs.
     */
    @PostConstruct
    public void init() {
        refreshDashboard();
        logger.info("AdminDashboardBean initialized. Found {} pending requests.", 
                    pendingProfiles != null ? pendingProfiles.size() : 0);
    }

    /**
     * Refreshes all dashboard datasets.
     * Triggered on initial load, after action completions, and via real-time Ajax polling.
     */
    public void refreshDashboard() {
        this.statistics = adminService.getDashboardStatistics();
        this.pendingProfiles = adminService.getPendingRequests();
        this.recentEmails = emailService.getRecentNotifications();

        // Preserve an active search filter across the background poll, otherwise
        // the periodic refresh would silently discard the administrator's results.
        if (isSearchActive()) {
            this.allProfiles = profileService.searchProfiles(this.searchQuery.trim());
        } else {
            this.allProfiles = adminService.getAllRequests();
        }
    }

    /**
     * Filters the applicant registry by name, email or tracking code.
     * Backed by {@link ProfileService#searchProfiles(String)}.
     */
    public void searchProfiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        String query = this.searchQuery == null ? "" : this.searchQuery.trim();

        if (query.isEmpty()) {
            clearSearch();
            return;
        }

        this.searchQuery = query;
        this.searchActive = true;
        this.allProfiles = profileService.searchProfiles(query);

        logger.info("Admin searched registry with query '{}' - {} match(es)", query, allProfiles.size());
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_INFO,
            "Search Complete",
            "Found " + allProfiles.size() + " matching profile(s) for \"" + query + "\"."
        ));
    }

    /**
     * Clears the active registry filter and restores the full applicant list.
     */
    public void clearSearch() {
        this.searchQuery = null;
        this.searchActive = false;
        refreshDashboard();
    }

    /**
     * Derives the tab count badge for the given tab identifier.
     *
     * @param tab Tab id: "pending", "all" or "emails"
     * @return Row count backing that tab
     */
    public int getTabCount(String tab) {
        if ("pending".equals(tab)) {
            return pendingProfiles == null ? 0 : pendingProfiles.size();
        }
        if ("all".equals(tab)) {
            return allProfiles == null ? 0 : allProfiles.size();
        }
        if ("emails".equals(tab)) {
            return recentEmails == null ? 0 : recentEmails.size();
        }
        return 0;
    }

    /**
     * Action to approve and verify an applicant profile.
     * 
     * @param profileId Primary database ID of profile
     */
    public void verifyProfile(Long profileId) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            logger.info("Admin initiated verification for profile ID: {}", profileId);
            adminService.verifyProfile(profileId);

            // Add UI notification
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Profile Verified",
                "Profile has been approved and confirmation email dispatched to user."
            ));

            // Reload data
            refreshDashboard();

        } catch (Exception ex) {
            logger.error("Failed to verify profile #{}: {}", profileId, ex.getMessage(), ex);
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Verification Failed",
                ex.getMessage()
            ));
        }
    }

    /**
     * Prepares rejection modal for a specific user profile.
     * 
     * @param profile User profile to cancel
     */
    public void prepareReject(UserProfile profile) {
        this.selectedProfileForAction = profile;
        this.rejectionReason = "";
        this.showRejectModal = true;
        logger.info("Opened rejection modal for profile: {}", profile.getTrackingCode());
    }

    /**
     * Confirms profile cancellation with the entered reason.
     */
    public void confirmReject() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Validate reason
        if (this.rejectionReason == null || this.rejectionReason.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Reason Required",
                "Please enter an explicit reason explaining why this verification is cancelled."
            ));
            return;
        }

        try {
            logger.info("Admin confirmed cancellation for profile ID: {} with reason: {}", 
                        selectedProfileForAction.getId(), rejectionReason);

            adminService.rejectProfile(selectedProfileForAction.getId(), this.rejectionReason.trim());

            // Close modal
            this.showRejectModal = false;
            this.selectedProfileForAction = null;
            this.rejectionReason = "";

            // Add UI notification
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Verification Cancelled",
                "Profile marked as cancelled and explanation email dispatched to user."
            ));

            // Reload data
            refreshDashboard();

        } catch (Exception ex) {
            logger.error("Failed to cancel profile: {}", ex.getMessage(), ex);
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Cancellation Failed",
                ex.getMessage()
            ));
        }
    }

    /**
     * Closes rejection modal without taking action.
     */
    public void closeRejectModal() {
        this.showRejectModal = false;
        this.selectedProfileForAction = null;
        this.rejectionReason = "";
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public DashboardStatisticsDto getStatistics() {
        return statistics;
    }

    public List<UserProfile> getPendingProfiles() {
        return pendingProfiles;
    }

    public List<UserProfile> getAllProfiles() {
        return allProfiles;
    }

    public List<EmailNotificationLog> getRecentEmails() {
        return recentEmails;
    }

    public UserProfile getSelectedProfileForAction() {
        return selectedProfileForAction;
    }

    public void setSelectedProfileForAction(UserProfile selectedProfileForAction) {
        this.selectedProfileForAction = selectedProfileForAction;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isShowRejectModal() {
        return showRejectModal;
    }

    public void setShowRejectModal(boolean showRejectModal) {
        this.showRejectModal = showRejectModal;
    }

    public String getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(String activeTab) {
        this.activeTab = activeTab;
    }

    /**
     * Action used by the dashboard tab bar to switch the visible panel.
     *
     * @param tab Tab id: "pending", "all" or "emails"
     */
    public void selectTab(String tab) {
        if (tab != null && !tab.trim().isEmpty()) {
            this.activeTab = tab.trim();
            logger.debug("Admin dashboard switched to tab: {}", this.activeTab);
        }
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public boolean isSearchActive() {
        return searchActive && searchQuery != null && !searchQuery.trim().isEmpty();
    }
}
