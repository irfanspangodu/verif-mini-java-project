package com.verif.feature.admin.dto;

import java.io.Serializable;

/**
 * Data Transfer Object aggregating verification metrics for display
 * in the Bento Grid Command Center tiles on the Admin Dashboard.
 */
public class DashboardStatisticsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Total applicant profiles registered in system
    private long totalProfiles;

    // Profiles currently awaiting verification
    private long pendingProfiles;

    // Profiles successfully approved and verified
    private long verifiedProfiles;

    // Profiles cancelled or rejected by administrator
    private long rejectedProfiles;

    // Calculated percentage of verified profiles
    private double verificationRate;

    /**
     * Default no-arg constructor.
     */
    public DashboardStatisticsDto() {
    }

    /**
     * Parameterized constructor computing approval percentage automatically.
     */
    public DashboardStatisticsDto(long totalProfiles, long pendingProfiles, 
                                  long verifiedProfiles, long rejectedProfiles) {
        this.totalProfiles = totalProfiles;
        this.pendingProfiles = pendingProfiles;
        this.verifiedProfiles = verifiedProfiles;
        this.rejectedProfiles = rejectedProfiles;
        
        long processed = verifiedProfiles + rejectedProfiles;
        if (processed > 0) {
            this.verificationRate = Math.round(((double) verifiedProfiles / processed * 100.0) * 10.0) / 10.0;
        } else {
            this.verificationRate = 0.0;
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public long getTotalProfiles() {
        return totalProfiles;
    }

    public void setTotalProfiles(long totalProfiles) {
        this.totalProfiles = totalProfiles;
    }

    public long getPendingProfiles() {
        return pendingProfiles;
    }

    public void setPendingProfiles(long pendingProfiles) {
        this.pendingProfiles = pendingProfiles;
    }

    public long getVerifiedProfiles() {
        return verifiedProfiles;
    }

    public void setVerifiedProfiles(long verifiedProfiles) {
        this.verifiedProfiles = verifiedProfiles;
    }

    public long getRejectedProfiles() {
        return rejectedProfiles;
    }

    public void setRejectedProfiles(long rejectedProfiles) {
        this.rejectedProfiles = rejectedProfiles;
    }

    public double getVerificationRate() {
        return verificationRate;
    }

    public void setVerificationRate(double verificationRate) {
        this.verificationRate = verificationRate;
    }
}
