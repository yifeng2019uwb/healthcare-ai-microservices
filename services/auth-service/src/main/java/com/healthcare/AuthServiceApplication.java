package com.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auth Service Application
 *
 * JWT Validation Service for Healthcare AI Microservices
 * - Validates JWT tokens from external auth providers
 * - Extracts user context for business services
 * - Stateless authentication service
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
