package com.fploptimizer.fplai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration class that defines security settings for the application.
 *
 * <p>This configuration includes HTTP security settings such as request authorization,
 * form login, logout, CSRF protection, and CORS handling.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application.
     *
     * <p>This method sets up security rules for different endpoints, enabling or disabling
     * features like authentication, CSRF protection, and CORS.</p>
     *
     * @param http the {@link HttpSecurity} object used to configure security.
     * @return a {@link SecurityFilterChain} instance that represents the configured security filter chain.
     * @throws Exception if there is an error in configuring security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Allow public access to the specified API endpoints
                        .requestMatchers("/api/players/search").permitAll()
                        .requestMatchers("/api/teams/optimize").permitAll()
                        .requestMatchers("/api/players").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        // Custom login page configuration
                        .loginPage("/login").permitAll()
                )
                .logout(LogoutConfigurer::permitAll) // Allow logout for all users
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection for API endpoints
                .cors(withDefaults()); // Enable CORS based on global configuration

        return http.build();
    }
}
