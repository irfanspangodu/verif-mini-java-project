package com.verif.feature.profile.repository;

import com.verif.core.constant.ProfileStatus;
import com.verif.feature.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for {@link UserProfile}.
 * 
 * Provides declarative and custom JPQL query methods for retrieval,
 * verification state filtering, and tracking code lookups.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Finds a user profile by its unique public tracking code.
     * 
     * @param trackingCode Unique tracking string (e.g., VRF-2026-X99AB)
     * @return Optional containing the profile if found
     */
    Optional<UserProfile> findByTrackingCode(String trackingCode);

    /**
     * Checks if a tracking code already exists to guarantee uniqueness.
     * 
     * @param trackingCode Code string
     * @return true if exists, false otherwise
     */
    boolean existsByTrackingCode(String trackingCode);

    /**
     * Finds profiles matching a specific verification status, ordered by newest first.
     * 
     * @param status Verification status enum (e.g. PENDING)
     * @return List of matching user profiles
     */
    List<UserProfile> findByVerificationStatusOrderByCreatedAtDesc(ProfileStatus status);

    /**
     * Finds all profiles ordered by creation timestamp descending.
     * 
     * @return All profiles ordered newest first
     */
    List<UserProfile> findAllByOrderByCreatedAtDesc();

    /**
     * Counts the total number of profiles having a specific status.
     * 
     * @param status Verification status
     * @return Total count
     */
    long countByVerificationStatus(ProfileStatus status);

    /**
     * Custom search query matching name, email, or tracking code.
     * 
     * @param query Search term
     * @return Matching profiles
     */
    @Query("SELECT u FROM UserProfile u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.trackingCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY u.createdAt DESC")
    List<UserProfile> searchProfiles(@Param("query") String query);
}
