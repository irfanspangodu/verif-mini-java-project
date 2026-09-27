package com.verif.feature.admin.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Spring MVC Controller that replaces the default Spring Boot "Whitelabel Error
 * Page" with a branded error page (`/WEB-INF/error.xhtml` - served through the
 * JSF FacesServlet wildcard mapping `*.xhtml`).
 *
 * <p>Without this controller any unhandled exception, Facelets parse error, EL
 * resolution failure, or 404 results in an unstyled Spring Whitelabel page
 * ("This application has no explicit mapping for /error") which is confusing
 * to end users and exposes the Spring internals.</p>
 */
@Controller
public class VerifErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(VerifErrorController.class);

    /**
     * Default MVC handler for the Spring Boot {@code /error} path.
     *
     * <p>Implementation strategy: because the JSF FacesServlet is registered on
     * {@code *.xhtml}, a plain Spring MVC view name cannot directly render the
     * Facelets template at {@code /error.xhtml}. Instead we instruct the browser
     * to request {@code /error.xhtml} and the FacesServlet picks it up as any
     * normal JSF page. The error details are preserved in the HTTP session so
     * they survive the redirect and can be rendered inside the JSF page.</p>
     *
     * @param request the original servlet request with
     *                {@link RequestDispatcher#ERROR_STATUS_CODE} and
     *                {@link RequestDispatcher#ERROR_MESSAGE} attributes
     *                populated by the Spring Boot error filter
     * @return a 302 redirect to the JSF-branded {@code /error.xhtml} page
     */
    @GetMapping("/error")
    public RedirectView handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object messageAttr = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exceptionAttr = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object pathAttr = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = (statusAttr instanceof Integer) ? (Integer) statusAttr
                                                          : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = null;
        if (messageAttr instanceof String && !((String) messageAttr).isBlank()) {
            message = (String) messageAttr;
        } else if (exceptionAttr instanceof Throwable) {
            message = ((Throwable) exceptionAttr).getMessage();
        }

        if (message == null || message.isBlank()) {
            message = HttpStatus.valueOf(statusCode).getReasonPhrase();
        }

        logger.warn("Intercepted Spring Boot error: status={}, message={}, path={}",
                    statusCode, message, pathAttr);

        // Preserve error state across the redirect to /error.xhtml (JSF side)
        request.getSession().setAttribute("verif.error.status", statusCode);
        request.getSession().setAttribute("verif.error.message", message);
        request.getSession().setAttribute("verif.error.path",
                pathAttr == null ? "(unknown)" : pathAttr.toString());

        return new RedirectView("/error.xhtml");
    }
}
