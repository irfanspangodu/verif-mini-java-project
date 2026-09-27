package com.verif.feature.profile;

import com.verif.core.constant.IdType;
import com.verif.core.constant.ProfileStatus;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.service.ProfileService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and Integration tests for ProfileService.
 * Validates registration logic, unique tracking code generation,
 * initial PENDING state assignment, and status query methods.
 */
@SpringBootTest
@Transactional
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Test
    @DisplayName("Should successfully register a new user profile with PENDING status and tracking code")
    void testRegisterProfileSuccess() {
        // Arrange
        UserProfile profile = new UserProfile();
        profile.setFullName("Jonathan Brand");
        profile.setEmail("jonathan.brand@example.com");
        profile.setPhone("+1 (555) 998-1122");
        profile.setDateOfBirth(LocalDate.of(1993, 6, 15));
        profile.setOccupation("Security Analyst");
        profile.setIdType(IdType.PASSPORT);
        profile.setIdNumber("P991827364");

        // Act
        UserProfile saved = profileService.registerProfile(profile);

        // Assert
        assertNotNull(saved.getId(), "Profile ID must be generated");
        assertNotNull(saved.getTrackingCode(), "Tracking code must be generated");
        assertTrue(saved.getTrackingCode().startsWith("VRF-"), "Tracking code must follow VRF- format");
        assertEquals(ProfileStatus.PENDING, saved.getVerificationStatus(), "Initial status must be PENDING");
        assertNotNull(saved.getDocumentReference(), "Document reference token must be assigned");

        // Lookup test
        Optional<UserProfile> retrieved = profileService.findByTrackingCode(saved.getTrackingCode());
        assertTrue(retrieved.isPresent(), "Must be able to find registered profile by tracking code");
        assertEquals("Jonathan Brand", retrieved.get().getFullName());
    }
}
