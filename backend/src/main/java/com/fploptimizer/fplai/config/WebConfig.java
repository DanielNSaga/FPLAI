package com.fploptimizer.fplai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for setting up CORS (Cross-Origin Resource Sharing) in the application.
 *
 * <p>This class configures CORS settings for the application, allowing the frontend to make requests
 * to the backend API from different origins.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings for the application.
     *
     * <p>This method allows requests from the specified origins to access the API endpoints under `/api/**`.
     * It defines which HTTP methods are allowed, enables credentials (such as cookies or HTTP authentication),
     * and specifies which headers can be included in the requests and responses.</p>
     *
     * @param registry the {@link CorsRegistry} used to register CORS mappings.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Allow requests from the specified origin
                .allowedOrigins("https://fplai.vercel.app")
                // Allow the following HTTP methods
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Allow credentials such as cookies or HTTP authentication
                .allowCredentials(true)
                // Allow all headers in the requests
                .allowedHeaders("*")
                // Expose specific headers in the response, such as "Authorization"
                .exposedHeaders("Authorization");
    }
}
