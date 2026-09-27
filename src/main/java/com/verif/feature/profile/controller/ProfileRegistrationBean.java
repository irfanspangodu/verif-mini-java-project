package com.verif.feature.profile.controller;

import com.verif.core.constant.IdType;
import com.verif.feature.profile.model.UserProfile;
import com.verif.feature.profile.service.ProfileService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * JSF Managed Bean handling the user registration and profile submission workflow.
 * 
 * ViewScoped controller that receives user input from `register.xhtml`,
 * triggers profile creation via {@link ProfileService}, and navigates to the
 * real-time status tracker upon completion.
 */
@Named("profileRegistrationBean")
@ViewScoped
public class ProfileRegistrationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ProfileRegistrationBean.class);

    // Injected business service for profile operations
    private final ProfileService profileService;

    // UserProfile backing model bound to input components
    private UserProfile profile;

    // Available ID document types for dropdown selection
    private IdType[] idTypes;

    // Generated tracking code after successful submission
    private String generatedTrackingCode;

    /**
     * Constructor injection of ProfileService.
     * 
     * @param profileService Injected ProfileService
     */
    @Autowired
    public ProfileRegistrationBean(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Post-construct initialization hook.
     * Prepares empty profile model and document type options.
     */
    @PostConstruct
    public void init() {
        this.profile = new UserProfile();
        this.idTypes = IdType.values();
        logger.info("ProfileRegistrationBean initialized with empty profile.");
    }

    /**
     * Action method triggered when user clicks the "Submit for Verification" button.
     * Validates input, saves to database, and initiates email notifications.
     * 
     * @return Navigation outcome or null for AJAX update
     */
    public String submitProfile() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            logger.info("Submitting verification profile for: {}", profile.getEmail());

            // Persist profile and dispatch notification alerts
            UserProfile saved = profileService.registerProfile(this.profile);

            // Record the generated code so it can be handed to the status page
            this.generatedTrackingCode = saved.getTrackingCode();

            // Flash global success message
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Profile Submitted Successfully!",
                "Your application has entered verification queue. Status: Verification Pending."
            ));

            // Redirect to the real-time status tracker with the tracking code in the
            // query string, keeping the status page deep-linkable and bookmarkable.
            //
            // BUGFIX: the separator must be a literal '&'. The previous "&amp;"
            // form is an XML entity which leaked into the generated URL and
            // produced a request parameter named "amp;trackingCode", so the
            // status page received no tracking code at all.
            String encodedCode = URLEncoder.encode(this.generatedTrackingCode, StandardCharsets.UTF_8);
            return "/status.xhtml?faces-redirect=true&trackingCode=" + encodedCode + "&newSubmission=true";

        } catch (Exception ex) {
            logger.error("Error submitting profile: {}", ex.getMessage(), ex);
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Submission Failed",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred. Please try again."
            ));
            return null;
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public IdType[] getIdTypes() {
        return idTypes;
    }

    public String getGeneratedTrackingCode() {
        return generatedTrackingCode;
    }
}
