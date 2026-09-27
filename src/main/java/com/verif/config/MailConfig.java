package com.verif.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Mail Subsystem and Asynchronous Task Configuration.
 * 
 * Supplies the dedicated email thread pool consumed by
 * EmailServiceImpl so user registration and admin verification workflows
 * experience zero latency during SMTP handshakes.
 */
@Configuration
@EnableAsync
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    /**
     * Dedicated ThreadPoolTaskExecutor for background email dispatches.
     * Prevents mail network latency from blocking the main HTTP request thread.
     * 
     * @return Executor configured with thread pooling parameters
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        // Initialize Spring's ThreadPoolTaskExecutor
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Base core threads kept active
        executor.setCorePoolSize(2);

        // Maximum threads allocated during notification spikes
        executor.setMaxPoolSize(10);

        // Capacity of dispatch queue buffer
        executor.setQueueCapacity(500);

        // Prefix for log identification
        executor.setThreadNamePrefix("VerifEmail-");

        // Initialize executor lifecycle
        executor.initialize();

        logger.info("Verif Mail Subsystem: Asynchronous emailTaskExecutor initialized successfully.");
        return executor;
    }
}
