-- ============================================================================
-- Verif - Full-Stack Profile Verification System Database Schema
-- Compatible with MySQL 8.0+ and MariaDB
-- ============================================================================

-- Create Database if it does not already exist
CREATE DATABASE IF NOT EXISTS `verif_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `verif_db`;

-- Drop existing tables to support clean initialization if requested
DROP TABLE IF EXISTS `email_notifications`;
DROP TABLE IF EXISTS `user_profiles`;

-- ----------------------------------------------------------------------------
-- Table: user_profiles
-- Stores all applicant identity details and their verification lifecycle state
-- ----------------------------------------------------------------------------
CREATE TABLE `user_profiles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Unique internal primary key identifier',
    `tracking_code` VARCHAR(32) NOT NULL COMMENT 'Unique external tracking code generated for applicant status checks',
    `full_name` VARCHAR(100) NOT NULL COMMENT 'Legal full name of applicant',
    `email` VARCHAR(150) NOT NULL COMMENT 'Primary registered email address for notification delivery',
    `phone` VARCHAR(25) NOT NULL COMMENT 'Contact telephone number',
    `date_of_birth` DATE NOT NULL COMMENT 'Applicant date of birth for identity confirmation',
    `occupation` VARCHAR(100) NOT NULL COMMENT 'Current professional occupation or employment status',
    `id_type` VARCHAR(30) NOT NULL COMMENT 'Type of identity document presented: PASSPORT, NATIONAL_ID, DRIVERS_LICENSE',
    `id_number` VARCHAR(50) NOT NULL COMMENT 'Unique official identity document serial number',
    `document_reference` VARCHAR(255) DEFAULT NULL COMMENT 'Document upload reference ID or cryptographic verification hash',
    `verification_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Current lifecycle state: PENDING, VERIFIED, REJECTED',
    `rejection_reason` TEXT DEFAULT NULL COMMENT 'Explanatory reason provided by the reviewing administrator when cancelling/rejecting',
    `verified_at` DATETIME DEFAULT NULL COMMENT 'Timestamp when administrator officially approved or finalized verification',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when profile registration was submitted',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp of last modification',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_profiles_tracking_code` (`tracking_code`),
    INDEX `idx_user_profiles_email` (`email`),
    INDEX `idx_user_profiles_status` (`verification_status`),
    INDEX `idx_user_profiles_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Applicant profiles and verification statuses';

-- ----------------------------------------------------------------------------
-- Table: email_notifications
-- Audit log of all automated notification emails generated and dispatched
-- ----------------------------------------------------------------------------
CREATE TABLE `email_notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Unique notification log identifier',
    `profile_id` BIGINT DEFAULT NULL COMMENT 'Optional foreign reference to associated user profile',
    `recipient_email` VARCHAR(150) NOT NULL COMMENT 'Target recipient email address',
    `recipient_role` VARCHAR(20) NOT NULL COMMENT 'Target audience: USER or ADMIN',
    `email_type` VARCHAR(50) NOT NULL COMMENT 'Category of notification: REGISTRATION_PENDING, ADMIN_ALERT, PROFILE_VERIFIED, PROFILE_REJECTED',
    `subject` VARCHAR(255) NOT NULL COMMENT 'Subject line of delivered email message',
    `body_preview` TEXT NOT NULL COMMENT 'Text body excerpt or HTML rendered snippet of the message',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DELIVERED' COMMENT 'Delivery state: DELIVERED, PENDING, FAILED',
    `sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Exact timestamp of dispatch',
    PRIMARY KEY (`id`),
    INDEX `idx_notifications_recipient` (`recipient_email`),
    INDEX `idx_notifications_type` (`email_type`),
    INDEX `idx_notifications_sent_at` (`sent_at`),
    CONSTRAINT `fk_notifications_profile` FOREIGN KEY (`profile_id`) REFERENCES `user_profiles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit trail of system email dispatches';

-- ----------------------------------------------------------------------------
-- Sample Demonstration Seed Data
-- ----------------------------------------------------------------------------
INSERT INTO `user_profiles`
(`tracking_code`, `full_name`, `email`, `phone`, `date_of_birth`, `occupation`, `id_type`, `id_number`, `document_reference`, `verification_status`, `rejection_reason`, `verified_at`, `created_at`)
VALUES
('VRF-2026-K92A1B', 'Alexander Vance', 'alex.vance@example.com', '+1 (555) 234-8901', '1992-04-18', 'Lead Research Architect', 'PASSPORT', 'P89218209', 'DOC-REF-90218-SHA256', 'PENDING', NULL, NULL, NOW() - INTERVAL 2 HOUR),
('VRF-2026-M48D9E', 'Elena Rostova', 'elena.rostova@example.com', '+1 (555) 345-6712', '1988-11-03', 'Financial Security Consultant', 'NATIONAL_ID', 'NID-9923841', 'DOC-REF-44211-SHA256', 'PENDING', NULL, NULL, NOW() - INTERVAL 45 MINUTE),
('VRF-2026-V11B7C', 'Marcus Chen', 'marcus.chen@example.com', '+1 (555) 456-7890', '1995-07-22', 'Data Systems Engineer', 'DRIVERS_LICENSE', 'DL-CA-4491028', 'DOC-REF-11029-SHA256', 'VERIFIED', NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 2 DAY),
('VRF-2026-X88F4R', 'Sophia Dubois', 'sophia.dubois@example.com', '+1 (555) 567-8901', '1990-09-14', 'Biomedical Specialist', 'PASSPORT', 'P44102931', 'DOC-REF-33918-SHA256', 'REJECTED', 'Identity document expired prior to application submission. Please submit valid, unexpired passport.', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 5 HOUR);

INSERT INTO `email_notifications`
(`profile_id`, `recipient_email`, `recipient_role`, `email_type`, `subject`, `body_preview`, `status`, `sent_at`)
VALUES
(1, 'alex.vance@example.com', 'USER', 'REGISTRATION_PENDING', 'Verif - Verification Pending (Ref: VRF-2026-K92A1B)', 'Hello Alexander, your profile has been submitted and is currently in verification review.', 'DELIVERED', NOW() - INTERVAL 2 HOUR),
(1, 'admin@verif-system.com', 'ADMIN', 'ADMIN_ALERT', 'Alert: New Verification Request Received (VRF-2026-K92A1B)', 'A new profile verification request has been submitted by Alexander Vance.', 'DELIVERED', NOW() - INTERVAL 2 HOUR),
(3, 'marcus.chen@example.com', 'USER', 'PROFILE_VERIFIED', 'Verif - Profile Successfully Verified (Ref: VRF-2026-V11B7C)', 'Congratulations Marcus! Your identity credentials have been thoroughly verified and approved.', 'DELIVERED', NOW() - INTERVAL 1 DAY),
(4, 'sophia.dubois@example.com', 'USER', 'PROFILE_REJECTED', 'Verif - Verification Notice (Ref: VRF-2026-X88F4R)', 'Dear Sophia, your profile verification could not be completed. Reason: Identity document expired.', 'DELIVERED', NOW() - INTERVAL 3 HOUR);
