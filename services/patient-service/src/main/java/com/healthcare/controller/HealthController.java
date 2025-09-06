package com.healthcare.controller;

import com.healthcare.constants.PatientServiceConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health Check Controller
 *
 * Provides health check endpoint for service monitoring.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@RestController
public class HealthController {

    @GetMapping(PatientServiceConstants.HEALTH_ENDPOINT)
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", PatientServiceConstants.SERVICE_STATUS_UP,
            "service", PatientServiceConstants.SERVICE_NAME,
            "version", PatientServiceConstants.SERVICE_VERSION,
            "timestamp", LocalDateTime.now().toString(),
            "database", PatientServiceConstants.SERVICE_STATUS_UP,
            "dependencies", Map.of(
                PatientServiceConstants.DEPENDENCY_DATABASE, PatientServiceConstants.SERVICE_STATUS_UP,
                PatientServiceConstants.DEPENDENCY_AUTH_SERVICE, PatientServiceConstants.SERVICE_STATUS_UP
            )
        );

        return ResponseEntity.ok(response);
    }
}
