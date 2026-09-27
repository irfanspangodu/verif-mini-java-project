-- Seed demonstration profiles if table is empty
INSERT INTO user_profiles (id, tracking_code, full_name, email, phone, date_of_birth, occupation, id_type, id_number, document_reference, verification_status, rejection_reason, verified_at, created_at, updated_at)
SELECT 1, 'VRF-2026-K92A1B', 'Alexander Vance', 'alex.vance@example.com', '+1 (555) 234-8901', '1992-04-18', 'Lead Research Architect', 'PASSPORT', 'P89218209', 'DOC-REF-90218-SHA256', 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE id = 1);

INSERT INTO user_profiles (id, tracking_code, full_name, email, phone, date_of_birth, occupation, id_type, id_number, document_reference, verification_status, rejection_reason, verified_at, created_at, updated_at)
SELECT 2, 'VRF-2026-M48D9E', 'Elena Rostova', 'elena.rostova@example.com', '+1 (555) 345-6712', '1988-11-03', 'Financial Security Consultant', 'NATIONAL_ID', 'NID-9923841', 'DOC-REF-44211-SHA256', 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE id = 2);

INSERT INTO user_profiles (id, tracking_code, full_name, email, phone, date_of_birth, occupation, id_type, id_number, document_reference, verification_status, rejection_reason, verified_at, created_at, updated_at)
SELECT 3, 'VRF-2026-V11B7C', 'Marcus Chen', 'marcus.chen@example.com', '+1 (555) 456-7890', '1995-07-22', 'Data Systems Engineer', 'DRIVERS_LICENSE', 'DL-CA-4491028', 'DOC-REF-11029-SHA256', 'VERIFIED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE id = 3);

INSERT INTO user_profiles (id, tracking_code, full_name, email, phone, date_of_birth, occupation, id_type, id_number, document_reference, verification_status, rejection_reason, verified_at, created_at, updated_at)
SELECT 4, 'VRF-2026-X88F4R', 'Sophia Dubois', 'sophia.dubois@example.com', '+1 (555) 567-8901', '1990-09-14', 'Biomedical Specialist', 'PASSPORT', 'P44102931', 'DOC-REF-33918-SHA256', 'REJECTED', 'Identity document expired prior to application submission. Please submit valid, unexpired passport.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE id = 4);

-- Seed initial notification outbox logs
INSERT INTO email_notifications (id, profile_id, recipient_email, recipient_role, email_type, subject, body_preview, status, sent_at)
SELECT 1, 1, 'alex.vance@example.com', 'USER', 'REGISTRATION_PENDING', 'Verif - Verification Pending (Ref: VRF-2026-K92A1B)', 'Hello Alexander, your profile has been submitted and is currently in verification review.', 'DELIVERED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM email_notifications WHERE id = 1);

INSERT INTO email_notifications (id, profile_id, recipient_email, recipient_role, email_type, subject, body_preview, status, sent_at)
SELECT 2, 1, 'admin@verif-system.com', 'ADMIN', 'ADMIN_ALERT', 'Alert: New Verification Request Received (VRF-2026-K92A1B)', 'A new profile verification request has been submitted by Alexander Vance.', 'DELIVERED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM email_notifications WHERE id = 2);

INSERT INTO email_notifications (id, profile_id, recipient_email, recipient_role, email_type, subject, body_preview, status, sent_at)
SELECT 3, 3, 'marcus.chen@example.com', 'USER', 'PROFILE_VERIFIED', 'Verif - Profile Successfully Verified (Ref: VRF-2026-V11B7C)', 'Congratulations Marcus! Your identity credentials have been thoroughly verified and approved.', 'DELIVERED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM email_notifications WHERE id = 3);

INSERT INTO email_notifications (id, profile_id, recipient_email, recipient_role, email_type, subject, body_preview, status, sent_at)
SELECT 4, 4, 'sophia.dubois@example.com', 'USER', 'PROFILE_REJECTED', 'Verif - Verification Notice (Ref: VRF-2026-X88F4R)', 'Dear Sophia, your profile verification could not be completed. Reason: Identity document expired prior to application submission.', 'DELIVERED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM email_notifications WHERE id = 4);
