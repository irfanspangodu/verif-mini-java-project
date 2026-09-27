package com.verif.feature.profile.service;

import com.verif.feature.profile.model.UserProfile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface encapsulating business logic for applicant profile registration,
 * status lookups, and profile queries.
 */
public interface ProfileService {

    /**
     * Registers a new applicant profile.
     * Generates tracking code, sets initial PENDING status, persists to database,
     * and dispatches initial confirmation and administrative alert notifications.
     * 
     * @param profile The UserProfile to register
     * @return Persisted UserProfile with generated identifiers and timestamps
     */
    UserProfile registerProfile(UserProfile profile);

    /**
     * Finds a profile by its public tracking code (e.g., VRF-2026-X99AB).
     * 
     * @param trackingCode Unique tracking code
     * @return Optional containing profile if located
     */
    Optional<UserProfile> findByTrackingCode(String trackingCode);

    /**
     * Finds a profile by its internal database ID.
     * 
     * @param id Database primary key
     * @return Optional containing profile if located
     */
    Optional<UserProfile> findById(Long id);

    /**
     * Retrieves all registered profiles ordered newest first.
     * 
     * @return List of all UserProfile records
     */
    List<UserProfile> getAllProfiles();

    /**
     * Retrieves all profiles currently in PENDING verification state.
     * 
     * @return List of pending UserProfile records
     */
    List<UserProfile> getPendingProfiles();

    /**
     * Searches profiles by applicant name, email, or tracking code.
     * 
     * @param query Search keyword
     * @return List of matching UserProfile records
     */
    List<UserProfile> searchProfiles(String query);
}
