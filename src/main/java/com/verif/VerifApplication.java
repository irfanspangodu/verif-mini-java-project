package com.verif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main application entry point for the Verif Profile Verification System.
 * 
 * Boots embedded JoinFaces + Jakarta Faces / Mojarra runtime on embedded Tomcat,
 * initializes Spring Data JPA, and activates asynchronous background notification executors.
 */
@SpringBootApplication
public class VerifApplication extends SpringBootServletInitializer {

    private static final Logger logger = LoggerFactory.getLogger(VerifApplication.class);

    /**
     * Supports WAR packaging when deployed to external Jakarta EE / Tomcat application servers.
     * 
     * @param builder SpringApplicationBuilder
     * @return Configured builder instance
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(VerifApplication.class);
    }

    /**
     * Standard Java main method executing standalone Spring Boot embedded container.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Launch Spring Boot application context
        SpringApplication.run(VerifApplication.class, args);
        logger.info("=================================================================");
        logger.info(" Verif Profile Verification System is RUNNING!");
        logger.info(" Web Application URL: http://localhost:8080");
        logger.info(" User Registration:   http://localhost:8080/register.xhtml");
        logger.info(" Status Tracker:      http://localhost:8080/status.xhtml");
        logger.info(" Admin Dashboard:     http://localhost:8080/admin/dashboard.xhtml");
        logger.info("=================================================================");
    }
}
