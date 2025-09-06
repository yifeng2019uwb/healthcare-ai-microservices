package com.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Patient Service Application
 *
 * Spring Boot application for managing patient profiles and medical information.
 * This service provides REST APIs for patient CRUD operations, medical history
 * management, and integration with other healthcare services.
 *
 * Key Features:
 * - Patient profile management
 * - Medical history tracking
 * - Insurance information management
 * - Emergency contact management
 * - Integration with appointment and medical record services
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@SpringBootApplication
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}
