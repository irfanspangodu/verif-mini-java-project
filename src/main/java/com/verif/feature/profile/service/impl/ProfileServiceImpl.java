package com.verif.feature.profile.service.impl;

import com.verif.core.constant.ProfileStatus;
import com.verif.core.exception.VerificationException;
import com.verif.core.util.VerificationCodeGenerator;
import com.verif.feature.notification.service.EmailService;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.repository.UserProfileRepository;
import com.verif.feature.profile.service.ProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation providing transactional business logic for applicant
 * profile registration and status retrieval.
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    // Repository for profile database operations
    private final UserProfileRepository profileRepository;

    // Service for dispatching real-time notification emails
    private final EmailService emailService;

    /**
     * Constructor injection for Spring managed dependencies.
     * 
     * @param profileRepository Database repository for UserProfile
     * @param emailService      Email dispatch service
     */
    @Autowired
    public ProfileServiceImpl(UserProfileRepository profileRepository, EmailService emailService) {
        this.profileRepository = profileRepository;
        this.emailService = emailService;
    }

    /**
     * Registers a new applicant profile into the Verif system.
     * Generates a collision-free tracking code, sets initial PENDING state,
     * persists to MySQL, and triggers background email notifications.
     */
    @Override
    @Transactional
    public UserProfile registerProfile(UserProfile profile) {
        // Step 1: Input sanity validation
        if (profile == null) {
            throw new VerificationException("Profile payload cannot be null");
        }

        // Step 2: Generate unique collision-resistant tracking code
        String trackingCode = generateUniqueTrackingCode();
        profile.setTrackingCode(trackingCode);

        // Step 3: Assign simulated document cryptographic reference
        if (profile.getDocumentReference() == null || profile.getDocumentReference().trim().isEmpty()) {
            profile.setDocumentReference(VerificationCodeGenerator.generateDocumentReference());
        }

        // Step 4: Ensure initial verification state is set to PENDING
        profile.setVerificationStatus(ProfileStatus.PENDING);
        profile.setRejectionReason(null);
        profile.setVerifiedAt(null);

        // Step 5: Persist profile to MySQL database
        UserProfile savedProfile = profileRepository.save(profile);
        logger.info("Successfully registered profile ID #{} with tracking code: {}", 
                    savedProfile.getId(), savedProfile.getTrackingCode());

        // Step 6: Dispatch automated "Verification Pending" email to user
        try {
            emailService.sendRegistrationPendingEmail(savedProfile);
        } catch (Exception ex) {
            logger.error("Failed to trigger pending email dispatch to user: {}", ex.getMessage());
        }

        // Step 7: Dispatch automated administrative alert email to admin
        try {
            emailService.sendAdminNewSubmissionAlert(savedProfile);
        } catch (Exception ex) {
            logger.error("Failed to trigger admin alert email: {}", ex.getMessage());
        }

        return savedProfile;
    }

    /**
     * Finds a user profile by public tracking code.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByTrackingCode(String trackingCode) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return profileRepository.findByTrackingCode(trackingCode.trim().toUpperCase());
    }

    /**
     * Finds a user profile by its primary key ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return profileRepository.findById(id);
    }

    /**
     * Retrieves all profiles ordered by creation timestamp descending.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getAllProfiles() {
        return profileRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves pending profiles waiting for administrative review.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getPendingProfiles() {
        return profileRepository.findByVerificationStatusOrderByCreatedAtDesc(ProfileStatus.PENDING);
    }

    /**
     * Searches profiles matching a keyword query.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> searchProfiles(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProfiles();
        }
        return profileRepository.searchProfiles(query.trim());
    }

    /**
     * Helper method to generate a collision-free tracking code against MySQL unique constraint.
     */
    private String generateUniqueTrackingCode() {
        String code;
        int attempts = 0;
        do {
            code = VerificationCodeGenerator.generateTrackingCode();
            attempts++;
            if (attempts > 20) {
                throw new VerificationException("Failed to generate unique tracking code after multiple attempts");
            }
        } while (profileRepository.existsByTrackingCode(code));
        return code;
    }
}
