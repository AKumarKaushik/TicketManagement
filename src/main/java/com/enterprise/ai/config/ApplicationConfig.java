package com.enterprise.ai.config;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    WebMvcConfigurer corsConfigurer(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/tickets/**")
                        .allowedOrigins(origins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                        .allowedHeaders("Content-Type", "Accept")
                        .allowCredentials(false);
            }
        };
    }
}
