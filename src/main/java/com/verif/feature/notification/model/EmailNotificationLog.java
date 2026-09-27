package com.verif.feature.notification.model;

import com.verif.core.constant.EmailType;
import com.verif.core.util.DateTimeUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an audit log entry of a sent or simulated email notification.
 * 
 * Stored in MySQL table `email_notifications` to provide full transparency,
 * traceability, and live audit monitoring directly within the Admin dashboard.
 */
@Entity
@Table(name = "email_notifications")
public class EmailNotificationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Associated profile identifier, if linked to a specific applicant.
     */
    @Column(name = "profile_id")
    private Long profileId;

    /**
     * Recipient email address.
     */
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    /**
     * Target recipient role: USER or ADMIN.
     */
    @Column(name = "recipient_role", nullable = false, length = 20)
    private String recipientRole;

    /**
     * Enumerated classification of email notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    /**
     * Email subject headline.
     */
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    /**
     * Excerpt or formatted content body of the delivered message.
     */
    @Column(name = "body_preview", nullable = false, columnDefinition = "TEXT")
    private String bodyPreview;

    /**
     * Delivery dispatch status: DELIVERED, PENDING, or FAILED.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "DELIVERED";

    /**
     * Exact timestamp of dispatch.
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * Default constructor for JPA.
     */
    public EmailNotificationLog() {
        // Required by JPA
    }

    /**
     * Parameterized constructor for quick log instantiation.
     */
    public EmailNotificationLog(Long profileId, String recipientEmail, String recipientRole, 
                                EmailType emailType, String subject, String bodyPreview, String status) {
        this.profileId = profileId;
        this.recipientEmail = recipientEmail;
        this.recipientRole = recipientRole;
        this.emailType = emailType;
        this.subject = subject;
        this.bodyPreview = bodyPreview;
        this.status = status;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Pre-persist hook ensuring sentAt is populated.
     */
    @PrePersist
    protected void onSend() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    /**
     * Formats sentAt into a friendly string for JSF rendering.
     * 
     * @return Friendly formatted date-time string
     */
    @Transient
    public String getFormattedSentAt() {
        return DateTimeUtils.formatFriendly(sentAt);
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        this.emailType = emailType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
