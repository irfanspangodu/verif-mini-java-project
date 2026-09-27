package com.verif.feature.notification.controller;

import com.verif.feature.notification.model.EmailNotificationLog;
import com.verif.feature.notification.service.EmailService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

/**
 * JSF Controller exposing real-time notification feeds and email outbox logs.
 */
@Named("notificationBean")
@ViewScoped
public class NotificationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Email service for outbox retrieval
    private final EmailService emailService;

    // Cached notification logs
    private List<EmailNotificationLog> notifications;

    /**
     * Constructor injection.
     */
    @Autowired
    public NotificationBean(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * PostConstruct initialization.
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * Refreshes notification log list.
     */
    public void refresh() {
        this.notifications = emailService.getRecentNotifications();
    }

    public List<EmailNotificationLog> getNotifications() {
        return notifications;
    }
}
