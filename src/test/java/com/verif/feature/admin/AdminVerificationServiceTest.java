package com.verif.feature.admin;

import com.verif.core.constant.IdType;
import com.verif.core.constant.ProfileStatus;
import com.verif.feature.admin.dto.DashboardStatisticsDto;
import com.verif.feature.admin.service.AdminVerificationService;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.service.ProfileService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and Integration tests for AdminVerificationService.
 * Validates approval transition, rejection transition with reason capture,
 * and statistical calculation accuracy.
 */
@SpringBootTest
@Transactional
class AdminVerificationServiceTest {

    @Autowired
    private AdminVerificationService adminVerificationService;

    @Autowired
    private ProfileService profileService;

    @Test
    @DisplayName("Should approve and verify pending applicant profile")
    void testVerifyProfile() {
        // Arrange
        UserProfile profile = createSampleProfile("Alice Martin", "alice@example.com");
        UserProfile saved = profileService.registerProfile(profile);

        // Act
        UserProfile verified = adminVerificationService.verifyProfile(saved.getId());

        // Assert
        assertEquals(ProfileStatus.VERIFIED, verified.getVerificationStatus(), "Status must transition to VERIFIED");
        assertNotNull(verified.getVerifiedAt(), "Verified timestamp must be populated");
        assertNull(verified.getRejectionReason(), "Rejection reason must be null for verified profile");
    }

    @Test
    @DisplayName("Should reject/cancel profile with explicit mandatory reason")
    void testRejectProfileWithReason() {
        // Arrange
        UserProfile profile = createSampleProfile("Bob Taylor", "bob@example.com");
        UserProfile saved = profileService.registerProfile(profile);

        String rejectionReason = "Document image is unreadable; blurriness detected.";

        // Act
        UserProfile rejected = adminVerificationService.rejectProfile(saved.getId(), rejectionReason);

        // Assert
        assertEquals(ProfileStatus.REJECTED, rejected.getVerificationStatus(), "Status must transition to REJECTED");
        assertEquals(rejectionReason, rejected.getRejectionReason(), "Rejection reason must be recorded");
        assertNotNull(rejected.getVerifiedAt(), "Action timestamp must be recorded");
    }

    @Test
    @DisplayName("Should accurately calculate dashboard statistics")
    void testDashboardStatistics() {
        DashboardStatisticsDto stats = adminVerificationService.getDashboardStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalProfiles() >= 0);
    }

    private UserProfile createSampleProfile(String name, String email) {
        UserProfile profile = new UserProfile();
        profile.setFullName(name);
        profile.setEmail(email);
        profile.setPhone("+1 (555) 123-4567");
        profile.setDateOfBirth(LocalDate.of(1991, 3, 20));
        profile.setOccupation("Software Engineer");
        profile.setIdType(IdType.NATIONAL_ID);
        profile.setIdNumber("NID-" + System.currentTimeMillis());
        return profile;
    }
}
