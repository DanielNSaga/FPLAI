package com.fploptimizer.fplai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * Configuration class for setting up application-wide beans.
 * This configuration provides beans that are commonly used throughout the application,
 * such as RestTemplate and Gson.
 */
@Configuration
public class AppConfig {

    /**
     * Creates and configures a {@link RestTemplate} bean.
     *
     * <p>The {@link RestTemplate} is used for making HTTP requests, and this bean
     * allows it to be injected wherever it's needed within the application.</p>
     *
     * @return a configured {@link RestTemplate} instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates and configures a {@link Gson} bean.
     *
     * <p>The {@link Gson} bean is used for JSON serialization and deserialization.
     * This bean makes it easier to handle JSON data throughout the application.</p>
     *
     * @return a configured {@link Gson} instance.
     */
    @Bean
    public Gson gson() {
        return new Gson();
    }
}
