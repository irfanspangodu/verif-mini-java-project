package com.verif.feature.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * JSF ViewScoped backing bean for the branded {@code /error.xhtml} page.
 *
 * <p>Reads error details (HTTP status code, message, failing request URI) that
 * were written into the session by
 * {@link VerifErrorController#handleError(jakarta.servlet.http.HttpServletRequest)}
 * and formats them into UI-facing strings that match the premium-black
 * aesthetic of the site. Afterwards it removes the transient values from the
 * session so that a simple page refresh does not re-read the same stale
 * diagnostic information.</p>
 */
@Named("errorDisplayBean")
@ViewScoped
public class ErrorDisplayBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ErrorDisplayBean.class);

    private int httpStatus;
    private String statusTitle;
    private String message;
    private String requestPath;

    /**
     * Initialize bean with a safe default so a manual navigation to
     * {@code /error.xhtml} still renders something sensible (404).
     */
    @PostConstruct
    public void init() {
        this.httpStatus = 404;
        this.statusTitle = HttpStatus.NOT_FOUND.getReasonPhrase();
        this.message = "The page you requested could not be located.";
        this.requestPath = "";
    }

    /**
     * Runs as a JSF {@code <f:viewAction>} each time the error page is visited.
     * Pulls the diagnostic values placed by {@link VerifErrorController} out of
     * the session and clears them so the next request starts clean.
     */
    public void loadFromSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) return;
        ExternalContext external = context.getExternalContext();
        if (external == null) return;
        Map<String, Object> session = external.getSessionMap();

        try {
            Object status = session.remove("verif.error.status");
            Object message = session.remove("verif.error.message");
            Object path = session.remove("verif.error.path");

            if (status instanceof Integer) {
                this.httpStatus = (Integer) status;
                try {
                    this.statusTitle = HttpStatus.valueOf(this.httpStatus).getReasonPhrase();
                } catch (IllegalArgumentException unknownStatus) {
                    this.statusTitle = "Unexpected Error";
                }
            }
            if (message instanceof String && !((String) message).isBlank()) {
                this.message = (String) message;
            }
            if (path instanceof String) {
                this.requestPath = (String) path;
            }

            logger.debug("ErrorDisplayBean loaded HTTP {} from session attributes (path={})",
                         this.httpStatus, this.requestPath);
        } catch (Exception ex) {
            logger.warn("Failed to extract error details from session: {}", ex.getMessage(), ex);
        }
    }

    // ======================= Getters for the UI =======================

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestPath() {
        return requestPath;
    }
}
