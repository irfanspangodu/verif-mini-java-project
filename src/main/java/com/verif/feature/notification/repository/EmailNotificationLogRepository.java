package com.verif.feature.notification.repository;

import com.verif.feature.notification.model.EmailNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for {@link EmailNotificationLog}.
 * 
 * Provides querying mechanisms for monitoring recent email dispatches,
 * recipient lookups, and audit log presentation.
 */
@Repository
public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, Long> {

    /**
     * Retrieves the top 50 most recent email notification logs ordered newest first.
     * 
     * @return Recent notification logs
     */
    List<EmailNotificationLog> findTop50ByOrderBySentAtDesc();

    /**
     * Finds email notification logs for a specific recipient email address.
     * 
     * @param recipientEmail Recipient email address
     * @return List of matching notification logs
     */
    List<EmailNotificationLog> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);

    /**
     * Finds email notification logs associated with a specific profile ID.
     * 
     * @param profileId Profile primary key ID
     * @return List of matching notification logs
     */
    List<EmailNotificationLog> findByProfileIdOrderBySentAtDesc(Long profileId);
}
