package com.verif.feature.notification.service;

import com.verif.feature.notification.model.EmailNotificationLog;
import com.verif.feature.profile.model.UserProfile;

import java.util.List;

/**
 * Service contract defining the email notification system for Verif.
 * 
 * Handles automated dispatches for:
 * - User submission acknowledgement & "Verification Pending" notification.
 * - Admin submission alerts.
 * - Verification approval notices.
 * - Verification cancellation notices with detailed reasons.
 * - Outbox audit logs.
 */
public interface EmailService {

    /**
     * Dispatches a "Verification Pending" notification to the registered user.
     * 
     * @param profile The newly registered UserProfile
     */
    void sendRegistrationPendingEmail(UserProfile profile);

    /**
     * Dispatches an administrative alert notifying staff of a new submission.
     * 
     * @param profile The newly registered UserProfile
     */
    void sendAdminNewSubmissionAlert(UserProfile profile);

    /**
     * Dispatches an official verification approval notification to the user.
     * 
     * @param profile The approved UserProfile
     */
    void sendProfileVerifiedEmail(UserProfile profile);

    /**
     * Dispatches a cancellation/rejection notification to the user including the specific reason.
     * 
     * @param profile The rejected UserProfile
     * @param rejectionReason Explicit explanation entered by the administrator
     */
    void sendProfileRejectedEmail(UserProfile profile, String rejectionReason);

    /**
     * Retrieves the list of recent email notifications for real-time dashboard display.
     * 
     * @return List of recent EmailNotificationLog entries
     */
    List<EmailNotificationLog> getRecentNotifications();
}
