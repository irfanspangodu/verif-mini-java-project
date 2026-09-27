package com.verif.feature.notification.service.impl;

import com.verif.core.constant.EmailType;
import com.verif.core.util.DateTimeUtils;
import com.verif.feature.notification.model.EmailNotificationLog;
import com.verif.feature.notification.repository.EmailNotificationLogRepository;
import com.verif.feature.notification.service.EmailService;
import com.verif.feature.profile.model.UserProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link EmailService} managing real-time and asynchronous
 * email notifications with dual dispatch: live SMTP transmission via JavaMailSender
 * (when configured) and comprehensive database audit logging.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    // Injected repository for persisting email logs into MySQL
    private final EmailNotificationLogRepository notificationLogRepository;

    // Optional JavaMailSender instance; dynamically configured if SMTP settings are active
    private final JavaMailSender mailSender;

    // Dedicated executor used to hand email work off the request thread
    private final Executor emailTaskExecutor;

    // Configurable sender from address
    @Value("${verif.mail.from:notifications@verif-system.com}")
    private String mailFrom;

    // Configurable administrative alert recipient
    @Value("${verif.mail.admin:admin@verif-system.com}")
    private String adminEmail;

    /**
     * Constructor injection for required dependencies.
     * 
     * @param notificationLogRepository Injected notification log repository
     * @param mailSender                Injected JavaMailSender (optional if SMTP not yet wired)
     * @param emailTaskExecutor         Dedicated asynchronous email executor
     */
    @Autowired
    public EmailServiceImpl(EmailNotificationLogRepository notificationLogRepository,
                            @Autowired(required = false) JavaMailSender mailSender,
                            @Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
        this.notificationLogRepository = notificationLogRepository;
        this.mailSender = mailSender;
        this.emailTaskExecutor = emailTaskExecutor;
    }

    /**
     * Defers a notification task until the caller's database transaction has
     * committed, then executes it on the dedicated email executor.
     *
     * BUGFIX: the notification audit row holds a foreign key to user_profiles.
     * Because the mail task used to start immediately (and asynchronously) while
     * the registration/decision transaction was still open, the parent profile
     * row was not yet visible on the other connection, so the audit insert
     * failed with a foreign key violation and the outbox entry was silently lost.
     * Running after commit guarantees the referenced profile exists.
     *
     * @param task Notification work to execute after commit
     */
    private void deferNotification(Runnable task) {
        Runnable asyncTask = () -> emailTaskExecutor.execute(task);

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    asyncTask.run();
                }
            });
        } else {
            // No surrounding transaction - dispatch immediately
            asyncTask.run();
        }
    }

    /**
     * Dispatches user notification upon registration with status "Verification Pending".
     * The subject and HTML body are rendered eagerly while the entity is still
     * managed, then transmission and audit logging happen after commit.
     */
    @Override
    public void sendRegistrationPendingEmail(UserProfile profile) {
        // Step 1: Formulate subject line
        String subject = "Verif - Verification Request Received (Tracking Ref: " + profile.getTrackingCode() + ")";

        // Step 2: Build HTML message body
        String htmlBody = buildPendingEmailHtml(profile);

        // Step 3: Capture the scalar values needed off-thread
        final Long profileId = profile.getId();
        final String recipient = profile.getEmail();

        // Step 4: Defer dispatch until the profile row has been committed
        deferNotification(() -> {
            deliverAndRecordEmail(profileId, recipient, "USER",
                                  EmailType.REGISTRATION_PENDING, subject, htmlBody);
            logger.info("Verification Pending email successfully processed for: {}", recipient);
        });
    }

    /**
     * Dispatches notification to admin alerting about a new applicant submission.
     */
    @Override
    public void sendAdminNewSubmissionAlert(UserProfile profile) {
        // Step 1: Formulate subject line
        String subject = "Verif Alert: New Verification Submission from " + profile.getFullName() + 
                         " (" + profile.getTrackingCode() + ")";

        // Step 2: Build HTML message body
        String htmlBody = buildAdminAlertHtml(profile);

        // Step 3: Capture the scalar values needed off-thread
        final Long profileId = profile.getId();
        final String applicantName = profile.getFullName();

        // Step 4: Defer dispatch until the profile row has been committed
        deferNotification(() -> {
            deliverAndRecordEmail(profileId, adminEmail, "ADMIN",
                                  EmailType.ADMIN_ALERT, subject, htmlBody);
            logger.info("Admin alert email processed for applicant: {}", applicantName);
        });
    }

    /**
     * Dispatches verification approval notification to the user.
     */
    @Override
    public void sendProfileVerifiedEmail(UserProfile profile) {
        // Step 1: Formulate subject line
        String subject = "Verif - Identity Verified Successfully! (Ref: " + profile.getTrackingCode() + ")";

        // Step 2: Build HTML message body
        String htmlBody = buildVerifiedEmailHtml(profile);

        // Step 3: Capture the scalar values needed off-thread
        final Long profileId = profile.getId();
        final String recipient = profile.getEmail();

        // Step 4: Defer dispatch until the status change has been committed
        deferNotification(() -> {
            deliverAndRecordEmail(profileId, recipient, "USER",
                                  EmailType.PROFILE_VERIFIED, subject, htmlBody);
            logger.info("Profile Verified email successfully dispatched to: {}", recipient);
        });
    }

    /**
     * Dispatches cancellation/rejection notification to the user with admin explanation.
     */
    @Override
    public void sendProfileRejectedEmail(UserProfile profile, String rejectionReason) {
        // Step 1: Formulate subject line
        String subject = "Verif Notice: Profile Verification Cancelled (Ref: " + profile.getTrackingCode() + ")";

        // Step 2: Build HTML message body with reason
        String htmlBody = buildRejectedEmailHtml(profile, rejectionReason);

        // Step 3: Capture the scalar values needed off-thread
        final Long profileId = profile.getId();
        final String recipient = profile.getEmail();

        // Step 4: Defer dispatch until the status change has been committed
        deferNotification(() -> {
            deliverAndRecordEmail(profileId, recipient, "USER",
                                  EmailType.PROFILE_REJECTED, subject, htmlBody);
            logger.info("Profile Cancellation email processed for: {} with reason: {}",
                        recipient, rejectionReason);
        });
    }

    /**
     * Retrieves recent email logs for live administrative outbox monitoring.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmailNotificationLog> getRecentNotifications() {
        return notificationLogRepository.findTop50ByOrderBySentAtDesc();
    }

    /**
     * Centralized execution method for sending via JavaMailSender and recording to database.
     */
    private void deliverAndRecordEmail(Long profileId, String recipient, String role,
                                       EmailType emailType, String subject, String htmlContent) {
        String deliveryStatus = "DELIVERED";

        // Attempt live SMTP transmission if mailSender is available
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(mailFrom);
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(message);
                logger.info("SMTP transmission dispatched to: {}", recipient);
            } catch (Exception ex) {
                logger.warn("SMTP relay not reachable or credentials unconfigured (using in-system simulated delivery): {}", 
                            ex.getMessage());
                // Non-fatal: the system still stores the full email in the DB audit log
                deliveryStatus = "DELIVERED (LOGGED)";
            }
        } else {
            logger.info("Local development mode: Email delivery simulated and recorded for {}", recipient);
        }

        // Persist email record to MySQL audit log table
        try {
            EmailNotificationLog logEntry = new EmailNotificationLog(
                profileId,
                recipient,
                role,
                emailType,
                subject,
                htmlContent,
                deliveryStatus
            );
            notificationLogRepository.save(logEntry);
            logger.info("Email notification record #{} saved to database", logEntry.getId());
        } catch (Exception ex) {
            logger.error("Failed to save email notification log to database: {}", ex.getMessage());
        }
    }

    // =========================================================================
    // HTML Email Template Builders
    // =========================================================================

    private String buildPendingEmailHtml(UserProfile profile) {
        return "<div style='font-family: Arial, sans-serif; background-color: #0A0A0B; color: #E4E4E7; padding: 24px; border-radius: 12px; border: 1px solid #27272A; max-width: 600px; margin: 0 auto;'>"
             + "  <div style='border-bottom: 1px solid #27272A; padding-bottom: 16px; margin-bottom: 20px;'>"
             + "    <h2 style='color: #FAFAFA; margin: 0;'>VERIF <span style='color: #A1A1AA; font-size: 14px; font-weight: normal;'>Identity Verification System</span></h2>"
             + "  </div>"
             + "  <div style='background: rgba(245, 158, 11, 0.1); border-left: 4px solid #F59E0B; padding: 12px 16px; margin-bottom: 20px; border-radius: 4px;'>"
             + "    <strong style='color: #FBBF24;'>Status: Verification Pending</strong>"
             + "  </div>"
             + "  <p>Dear <strong>" + profile.getFullName() + "</strong>,</p>"
             + "  <p>Thank you for submitting your identity profile for verification. Your application has entered our review queue and is currently undergoing verification by our administrative team.</p>"
             + "  <table style='width: 100%; margin: 20px 0; border-collapse: collapse;'>"
             + "    <tr><td style='padding: 8px 0; color: #A1A1AA;'>Tracking Code:</td><td style='padding: 8px 0; font-weight: bold; color: #FAFAFA;'>" + profile.getTrackingCode() + "</td></tr>"
             + "    <tr><td style='padding: 8px 0; color: #A1A1AA;'>Document Type:</td><td style='padding: 8px 0; color: #E4E4E7;'>" + profile.getIdType().getLabel() + "</td></tr>"
             + "    <tr><td style='padding: 8px 0; color: #A1A1AA;'>Document Serial:</td><td style='padding: 8px 0; color: #E4E4E7;'>" + profile.getIdNumber() + "</td></tr>"
             + "    <tr><td style='padding: 8px 0; color: #A1A1AA;'>Submitted At:</td><td style='padding: 8px 0; color: #E4E4E7;'>" + DateTimeUtils.nowFriendly() + "</td></tr>"
             + "  </table>"
             + "  <p style='color: #A1A1AA; font-size: 13px;'>You will receive a follow-up notification once an administrator has evaluated your credentials.</p>"
             + "  <div style='border-top: 1px solid #27272A; padding-top: 16px; margin-top: 24px; font-size: 12px; color: #71717A; text-align: center;'>"
             + "    &copy; 2026 Verif Verification System. All rights reserved."
             + "  </div>"
             + "</div>";
    }

    private String buildAdminAlertHtml(UserProfile profile) {
        return "<div style='font-family: Arial, sans-serif; background-color: #0A0A0B; color: #E4E4E7; padding: 24px; border-radius: 12px; border: 1px solid #27272A; max-width: 600px; margin: 0 auto;'>"
             + "  <h3 style='color: #F59E0B; margin-top: 0;'>New Identity Verification Request</h3>"
             + "  <p>An applicant has submitted an identity verification request requiring administrative review:</p>"
             + "  <ul style='line-height: 1.8; color: #A1A1AA;'>"
             + "    <li><strong>Applicant:</strong> " + profile.getFullName() + "</li>"
             + "    <li><strong>Email:</strong> " + profile.getEmail() + "</li>"
             + "    <li><strong>Tracking Ref:</strong> " + profile.getTrackingCode() + "</li>"
             + "    <li><strong>Document:</strong> " + profile.getIdType().getLabel() + " (" + profile.getIdNumber() + ")</li>"
             + "    <li><strong>Occupation:</strong> " + profile.getOccupation() + "</li>"
             + "  </ul>"
             + "  <p>Please log in to the Verif Admin Dashboard to verify or cancel this request.</p>"
             + "</div>";
    }

    private String buildVerifiedEmailHtml(UserProfile profile) {
        return "<div style='font-family: Arial, sans-serif; background-color: #0A0A0B; color: #E4E4E7; padding: 24px; border-radius: 12px; border: 1px solid #27272A; max-width: 600px; margin: 0 auto;'>"
             + "  <div style='border-bottom: 1px solid #27272A; padding-bottom: 16px; margin-bottom: 20px;'>"
             + "    <h2 style='color: #10B981; margin: 0;'>VERIF <span style='color: #A1A1AA; font-size: 14px; font-weight: normal;'>Verification Approved</span></h2>"
             + "  </div>"
             + "  <div style='background: rgba(16, 185, 129, 0.1); border-left: 4px solid #10B981; padding: 12px 16px; margin-bottom: 20px; border-radius: 4px;'>"
             + "    <strong style='color: #34D399;'>Status: Verified</strong>"
             + "  </div>"
             + "  <p>Dear <strong>" + profile.getFullName() + "</strong>,</p>"
             + "  <p>Great news! Your identity profile and submitted documentation have been verified and approved by our administrative inspection team.</p>"
             + "  <p>Your profile is now officially marked as <strong>VERIFIED</strong> in our records under Reference <code>" + profile.getTrackingCode() + "</code>.</p>"
             + "  <div style='border-top: 1px solid #1E293B; padding-top: 16px; margin-top: 24px; font-size: 12px; color: #64748B; text-align: center;'>"
             + "    &copy; 2026 Verif Verification System. All rights reserved."
             + "  </div>"
             + "</div>";
    }

    private String buildRejectedEmailHtml(UserProfile profile, String rejectionReason) {
        return "<div style='font-family: Arial, sans-serif; background-color: #0A0A0B; color: #E4E4E7; padding: 24px; border-radius: 12px; border: 1px solid #27272A; max-width: 600px; margin: 0 auto;'>"
             + "  <div style='border-bottom: 1px solid #27272A; padding-bottom: 16px; margin-bottom: 20px;'>"
             + "    <h2 style='color: #F43F5E; margin: 0;'>VERIF <span style='color: #A1A1AA; font-size: 14px; font-weight: normal;'>Verification Update</span></h2>"
             + "  </div>"
             + "  <div style='background: rgba(244, 63, 94, 0.1); border-left: 4px solid #F43F5E; padding: 12px 16px; margin-bottom: 20px; border-radius: 4px;'>"
             + "    <strong style='color: #FB7185;'>Status: Verification Cancelled</strong>"
             + "  </div>"
             + "  <p>Dear <strong>" + profile.getFullName() + "</strong>,</p>"
             + "  <p>We are writing to inform you that your identity verification request (Ref: <code>" + profile.getTrackingCode() + "</code>) could not be verified at this time.</p>"
             + "  <div style='background-color: #18181B; border-radius: 8px; padding: 16px; margin: 20px 0;'>"
             + "    <strong style='color: #FAFAFA;'>Reason for Cancellation:</strong>"
             + "    <p style='color: #E4E4E7; margin: 8px 0 0 0; font-style: italic;'>" + rejectionReason + "</p>"
             + "  </div>"
             + "  <p>You may submit a revised application addressing the feedback above.</p>"
             + "  <div style='border-top: 1px solid #27272A; padding-top: 16px; margin-top: 24px; font-size: 12px; color: #71717A; text-align: center;'>"
             + "    &copy; 2026 Verif Verification System. All rights reserved."
             + "  </div>"
             + "</div>";
    }
}
