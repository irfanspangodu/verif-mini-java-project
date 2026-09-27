package com.verif.feature.admin.service.impl;

import com.verif.core.constant.ProfileStatus;
import com.verif.core.exception.VerificationException;
import com.verif.feature.admin.dto.DashboardStatisticsDto;
import com.verif.feature.admin.service.AdminVerificationService;
import com.verif.feature.notification.service.EmailService;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.repository.UserProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation executing administrative profile verification,
 * cancellation with detailed reason tracking, and metric calculation.
 */
@Service
public class AdminVerificationServiceImpl implements AdminVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminVerificationServiceImpl.class);

    // Profile repository for database state transitions
    private final UserProfileRepository profileRepository;

    // Email service for dispatching verification decisions
    private final EmailService emailService;

    /**
     * Constructor injection for required dependencies.
     * 
     * @param profileRepository JPA repository for UserProfile
     * @param emailService      Email dispatch service
     */
    @Autowired
    public AdminVerificationServiceImpl(UserProfileRepository profileRepository, EmailService emailService) {
        this.profileRepository = profileRepository;
        this.emailService = emailService;
    }

    /**
     * Approves and verifies a pending user profile.
     */
    @Override
    @Transactional
    public UserProfile verifyProfile(Long profileId) {
        // Step 1: Validate input parameter
        if (profileId == null) {
            throw new VerificationException("Profile ID must not be null for verification");
        }

        // Step 2: Fetch profile from MySQL database
        UserProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new VerificationException("Profile not found with ID: " + profileId));

        // Step 3: Transition verification state to VERIFIED
        profile.setVerificationStatus(ProfileStatus.VERIFIED);
        profile.setVerifiedAt(LocalDateTime.now());
        profile.setRejectionReason(null); // Clear any prior rejection comments

        // Step 4: Persist status transition to MySQL
        UserProfile updatedProfile = profileRepository.save(profile);
        logger.info("Admin approved and verified profile ID: {} (Ref: {})", 
                    profile.getId(), profile.getTrackingCode());

        // Step 5: Dispatch verification approval notification to user
        try {
            emailService.sendProfileVerifiedEmail(updatedProfile);
        } catch (Exception ex) {
            logger.error("Failed to dispatch verification email to user: {}", ex.getMessage());
        }

        return updatedProfile;
    }

    /**
     * Cancels and denies a user profile with an explicit administrative reason.
     */
    @Override
    @Transactional
    public UserProfile rejectProfile(Long profileId, String rejectionReason) {
        // Step 1: Validate profile ID
        if (profileId == null) {
            throw new VerificationException("Profile ID must not be null for rejection");
        }

        // Step 2: Validate mandatory cancellation reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new VerificationException("A cancellation reason must be provided when rejecting a profile");
        }

        // Step 3: Retrieve profile from MySQL
        UserProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new VerificationException("Profile not found with ID: " + profileId));

        // Step 4: Update state and record cancellation details
        profile.setVerificationStatus(ProfileStatus.REJECTED);
        profile.setRejectionReason(rejectionReason.trim());
        profile.setVerifiedAt(LocalDateTime.now());

        // Step 5: Persist cancellation state to MySQL
        UserProfile updatedProfile = profileRepository.save(profile);
        logger.info("Admin rejected profile ID: {} with reason: '{}'", 
                    profile.getId(), rejectionReason.trim());

        // Step 6: Dispatch cancellation email with explicit reason to user
        try {
            emailService.sendProfileRejectedEmail(updatedProfile, rejectionReason.trim());
        } catch (Exception ex) {
            logger.error("Failed to dispatch cancellation email to user: {}", ex.getMessage());
        }

        return updatedProfile;
    }

    /**
     * Aggregates real-time verification statistics for the Bento Grid dashboard tiles.
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardStatisticsDto getDashboardStatistics() {
        // Fetch total profiles count
        long total = profileRepository.count();

        // Fetch counts partitioned by status
        long pending = profileRepository.countByVerificationStatus(ProfileStatus.PENDING);
        long verified = profileRepository.countByVerificationStatus(ProfileStatus.VERIFIED);
        long rejected = profileRepository.countByVerificationStatus(ProfileStatus.REJECTED);

        return new DashboardStatisticsDto(total, pending, verified, rejected);
    }

    /**
     * Retrieves all pending requests awaiting review.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getPendingRequests() {
        return profileRepository.findByVerificationStatusOrderByCreatedAtDesc(ProfileStatus.PENDING);
    }

    /**
     * Retrieves all requests for complete audit view.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getAllRequests() {
        return profileRepository.findAllByOrderByCreatedAtDesc();
    }
}
