package org.ithub.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-route", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .uri("lb://user-service"))

                // Post Service Routes
                .route("post-service-route", r -> r
                        .path("/api/post/**")
                        .uri("lb://post-service"))

                // Media Storage Service Routes
                .route("media-storage-route", r -> r
                        .path("/api/media/**")
                        .uri("lb://media-storage-service"))

                // Social Integration Service Routes
                .route("social-integration-route", r -> r
                        .path("/api/social/**")
                        .uri("lb://social-integration-service"))

                // Frontend static content (for production)
                .route("frontend-route", r -> r
                        .path("/", "/*.html", "/*.js", "/*.css", "/*.ico", "/static/**")
                        .uri("http://localhost:3000"))

                .build();
    }

    @Bean
    public CorsConfiguration corsConfiguration() {
        return new CorsConfiguration();
    }
}

class CorsConfiguration {
    // CORS configuration if needed
}