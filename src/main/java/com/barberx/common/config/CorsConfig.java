package com.barberx.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Enterprise-level centralized CORS configuration.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        
        // Allow frontend origins
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
        
        // Allow standard HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, auth headers)
        config.setAllowCredentials(true);
        
        // Apply to all paths
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}
