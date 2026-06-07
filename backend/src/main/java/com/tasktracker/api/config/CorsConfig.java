package com.tasktracker.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS = Cross-Origin Resource Sharing
// Without this, the browser would block our Vue frontend from calling the Spring Boot API
// because they run on different ports (5173 vs 8081)
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // allow any request to /api/** from our frontend dev servers
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",  // Vite dev server (Vue 3)
                        "http://localhost:8080"   // alternative port just in case
                )
                // allow all the HTTP methods we actually use
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")  // allow any headers (like Content-Type)
                .allowCredentials(true)
                .maxAge(3600);  // cache the preflight response for 1 hour
    }
}
