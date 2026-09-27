package com.verif.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * JavaServer Faces (JSF / Jakarta Faces) Context Configuration.
 * 
 * Sets up Facelets parameters, error handling modes, and resource optimizations
 * ensuring all files and assets load with high performance and zero overhead.
 * 
 * The project stage is property driven so production deployments can enable
 * Mojarra optimizations instead of running in Development mode.
 */
@Configuration
public class JsfConfig {

    private static final Logger logger = LoggerFactory.getLogger(JsfConfig.class);

    // JSF project stage: Development (verbose, hot reload) or Production (optimized)
    @Value("${verif.faces.project-stage:Development}")
    private String projectStage;

    // Facelets refresh period: 1 = recompile modified views, -1 = never (production)
    @Value("${verif.faces.facelets-refresh-period:1}")
    private String faceletsRefreshPeriod;

    /**
     * ServletContextInitializer configuring JSF Facelets and Mojarra parameters.
     * 
     * @return Initializer Lambda modifying ServletContext parameters
     */
    @Bean
    public ServletContextInitializer jsfServletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                // Set ProjectStage to Production or Development
                servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", projectStage);

                // Enable skipping comments in Facelets XHTML templates to minimize payload KB size
                servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", "true");

                // Set Facelets refresh period to -1 (production) or 1 (development hot-reload)
                servletContext.setInitParameter("jakarta.faces.FACELETS_REFRESH_PERIOD", faceletsRefreshPeriod);

                // Enable state saving on server to prevent heavy client-side view state transfer
                servletContext.setInitParameter("jakarta.faces.STATE_SAVING_METHOD", "server");

                // Interpret empty strings as null to align JSF bean properties with database constraints
                servletContext.setInitParameter("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");

                logger.info("Verif JSF Configuration: Facelets parameters and Mojarra initialized (projectStage={}).",
                            projectStage);
            }
        };
    }

    /**
     * WebMvcConfigurer redirecting the application root context to the Bento landing page.
     */
    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer forwardToIndex() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("redirect:/index.xhtml");
            }
        };
    }
}
