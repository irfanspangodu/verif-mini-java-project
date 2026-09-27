package com.verif.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;

/**
 * Enterprise database configuration for the Verif system.
 * 
 * Enables:
 * - Declarative JPA Repositories across all modular feature packages.
 * - Robust transactional boundary management via Spring Transaction Manager.
 * - Dual-profile support: Production MySQL Connector / Local in-memory Fallback.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.verif.feature.profile.repository",
    "com.verif.feature.notification.repository"
})
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * Diagnostic initialization hook verifying database subsystem startup.
     */
    @PostConstruct
    public void init() {
        // Log successful boot of database persistence subsystem
        logger.info("=================================================");
        logger.info("Verif System: Database Configuration Initialized");
        logger.info("JPA Repositories enabled for modular features");
        logger.info("=================================================");
    }
}
