package com.verif.feature.profile.model;

import com.verif.core.constant.IdType;
import com.verif.core.constant.ProfileStatus;
import com.verif.core.util.DateTimeUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an applicant's profile within the Verif system.
 * 
 * Mapped to the MySQL table `user_profiles`. Captures full identity information,
 * identity credentials, dynamic verification state, and administrative rejection remarks.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary internal database ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Unique alphanumeric tracking code generated for status lookups (e.g. VRF-2026-X99AB).
     */
    @NotBlank(message = "Tracking code is mandatory")
    @Column(name = "tracking_code", nullable = false, unique = true, length = 32)
    private String trackingCode;

    /**
     * Applicant's full legal name.
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Applicant's primary email address for status notices.
     */
    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /**
     * Applicant's contact phone number.
     */
    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false, length = 25)
    private String phone;

    /**
     * Applicant's date of birth.
     */
    @NotNull(message = "Date of birth is required")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Applicant's occupation or job title.
     */
    @NotBlank(message = "Occupation is required")
    @Column(name = "occupation", nullable = false, length = 100)
    private String occupation;

    /**
     * Type of government-issued identification document.
     */
    @NotNull(message = "Identification document type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false, length = 30)
    private IdType idType;

    /**
     * Official serial number or passport number of the identity document.
     */
    @NotBlank(message = "Identification document number is required")
    @Column(name = "id_number", nullable = false, length = 50)
    private String idNumber;

    /**
     * Generated reference token or cryptographic hash of submitted documents.
     */
    @Column(name = "document_reference", length = 255)
    private String documentReference;

    /**
     * Current lifecycle status of verification: PENDING, VERIFIED, or REJECTED.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private ProfileStatus verificationStatus = ProfileStatus.PENDING;

    /**
     * Detailed cancellation explanation provided by the administrator if rejected.
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Timestamp when verification was finalized (approved or rejected).
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last updated timestamp.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default no-arg constructor required by JPA and JSF specifications.
     */
    public UserProfile() {
        // Required by JPA specification
    }

    /**
     * Pre-persist lifecycle callback setting creation and modification timestamps.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.verificationStatus == null) {
            this.verificationStatus = ProfileStatus.PENDING;
        }
    }

    /**
     * Pre-update lifecycle callback updating modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // Getters and Setters with explanatory JavaDoc
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }

    public ProfileStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(ProfileStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // Derived (non-persistent) display helpers
    // =========================================================================

    /**
     * Friendly formatted submission timestamp for JSF rendering.
     * Without this the views rendered {@code LocalDateTime.toString()}, exposing
     * raw ISO values such as "2026-09-26T23:11:47.123" in the admin tables.
     * 
     * @return Friendly formatted timestamp, or "N/A" when unset
     */
    @Transient
    public String getFormattedCreatedAt() {
        return DateTimeUtils.formatFriendly(createdAt);
    }

    /**
     * Friendly formatted verification/decision timestamp for JSF rendering.
     * 
     * @return Friendly formatted timestamp, or "N/A" when unset
     */
    @Transient
    public String getFormattedVerifiedAt() {
        return DateTimeUtils.formatFriendly(verifiedAt);
    }
}
