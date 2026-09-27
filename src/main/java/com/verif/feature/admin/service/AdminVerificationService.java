package com.verif.feature.admin.service;

import com.verif.feature.admin.dto.DashboardStatisticsDto;
import com.verif.feature.profile.model.UserProfile;

import java.util.List;

/**
 * Service contract defining administrative workflows for inspecting,
 * approving, cancelling/rejecting identity verification requests, and
 * computing dashboard statistics.
 */
public interface AdminVerificationService {

    /**
     * Approves and verifies a pending user profile.
     * Transitions status to VERIFIED, timestamps action, and dispatches approval email.
     * 
     * @param profileId Database identifier of the profile to verify
     * @return Updated UserProfile record
     */
    UserProfile verifyProfile(Long profileId);

    /**
     * Cancels and denies a user profile with an explicit administrative reason.
     * Transitions status to REJECTED, persists reason, and dispatches rejection email.
     * 
     * @param profileId       Database identifier of the profile to cancel
     * @param rejectionReason Explicit explanation of the rejection for the user
     * @return Updated UserProfile record
     */
    UserProfile rejectProfile(Long profileId, String rejectionReason);

    /**
     * Aggregates real-time verification statistics for the Bento Grid dashboard tiles.
     * 
     * @return Populated DashboardStatisticsDto instance
     */
    DashboardStatisticsDto getDashboardStatistics();

    /**
     * Retrieves all pending verification requests awaiting administrative decision.
     * 
     * @return List of pending UserProfile records
     */
    List<UserProfile> getPendingRequests();

    /**
     * Retrieves all profiles regardless of status for administrative inspection.
     * 
     * @return List of all UserProfile records
     */
    List<UserProfile> getAllRequests();
}
