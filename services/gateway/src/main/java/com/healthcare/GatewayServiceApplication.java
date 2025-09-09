package com.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway Service Application
 *
 * API Gateway for Healthcare AI Microservices
 * - Routes requests to appropriate backend services
 * - Handles authentication and authorization
 * - Provides unified API interface
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
