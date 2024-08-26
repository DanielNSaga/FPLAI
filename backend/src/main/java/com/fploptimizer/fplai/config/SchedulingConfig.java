package com.fploptimizer.fplai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling for the application.
 *
 * <p>This configuration activates Spring's scheduling support, allowing
 * scheduled tasks to be executed at specified intervals or times.</p>
 *
 * <p>By annotating this class with {@link EnableScheduling}, you enable the detection of
 * `@Scheduled` annotations on any Spring-managed beans in the context.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No additional configuration is required here. The annotations handle everything.
}
