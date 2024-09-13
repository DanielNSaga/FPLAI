package com.fploptimizer.fplai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Security configuration class for setting up Spring Security in the application.
 *
 * <p>This class configures the security filter chain, CORS settings, user authentication, and password encoding.
 * It is used to define how the application handles security concerns such as authorization, authentication, and
 * CORS settings.
 *
 * <p>Highlights of the configuration:
 * <ul>
 *     <li>{@code @EnableWebSecurity} activates Spring Security's web security support.</li>
 *     <li>Basic authentication is disabled to avoid browser pop-ups for credentials.</li>
 *     <li>CSRF protection is disabled for APIs since the application uses stateless sessions.</li>
 *     <li>Session management is set to stateless to align with RESTful practices.</li>
 *     <li>An in-memory user store is used for authentication, with the credentials provided via application properties.</li>
 * </ul>
 *
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    /**
     * Configures the security filter chain.
     *
     * <p>This method sets up the security configuration for HTTP requests, including:
     * <ul>
     *     <li>CORS configuration using the method {@link #corsConfigurationSource()}.</li>
     *     <li>Exception handling to return HTTP 401 Unauthorized for unauthenticated requests.</li>
     *     <li>Disabling CSRF as the application is stateless.</li>
     *     <li>Using stateless session management to avoid session creation for APIs.</li>
     *     <li>Permitting all incoming requests.</li>
     *     <li>Disabling HTTP Basic Authentication.</li>
     * </ul>
     *
     * @param http the {@code HttpSecurity} object used to configure security for HTTP requests
     * @return the configured {@code SecurityFilterChain}
     * @throws Exception if there is a configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(c -> c.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions
                .authorizeHttpRequests(req -> req
                        .anyRequest().permitAll()) // Allow all requests
                .httpBasic(AbstractHttpConfigurer::disable); // Disable HTTP Basic Authentication

        return http.build();
    }

    /**
     * Configures an in-memory user details service for authentication.
     *
     * <p>This method creates an in-memory user based on the username and password
     * properties set in the configuration. The password is encoded using {@link BCryptPasswordEncoder}.
     *
     * @return the configured {@code UserDetailsService}
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder().encode(password))
                        .roles("USER")
                        .build()
        );
    }

    /**
     * Configures the password encoder.
     *
     * <p>This method returns a {@link BCryptPasswordEncoder} which is used to encode passwords securely.
     *
     * @return the configured {@code PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS settings for the application.
     *
     * <p>This method sets up the CORS configuration, including allowed origins, HTTP methods, headers,
     * and credentials. It specifies which domains can make cross-origin requests and what kind of
     * requests are permitted.
     *
     * <p>Note that the allowed origins in this configuration are specific and should be adjusted
     * based on the application's deployment.
     *
     * @return the configured {@code CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("https://fplai-1.onrender.com")); // Specific origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed HTTP methods
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Content-Type-Options", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")); // Allowed headers
        configuration.setAllowCredentials(true); // Allow credentials (e.g., cookies, authentication)
        configuration.setMaxAge(7200L); // Maximum age for CORS preflight response in seconds
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
