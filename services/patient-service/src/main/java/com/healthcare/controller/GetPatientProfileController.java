package com.healthcare.controller;

import com.healthcare.api.model.GetPatientProfileOutput;
import com.healthcare.constants.PatientServiceConstants;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Get Patient Profile operations.
 * Handles patient profile retrieval.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping(PatientServiceConstants.GET_PROFILE_ENDPOINT)
@CrossOrigin(origins = "*")
public class GetPatientProfileController {

    @Autowired
    private PatientService patientService;

    /**
     * Get patient profile.
     * GET /api/patients/profile
     *
     * @return the patient profile
     */
    @GetMapping
    public ResponseEntity<GetPatientProfileOutput> getPatientProfile() {
        try {
            // TODO: Extract user ID from JWT token
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // This should come from JWT

            User user = patientService.getUserById(userId);
            Patient patient = patientService.getPatientByUserId(userId);

            GetPatientProfileOutput response = buildPatientProfileResponse(user, patient);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Build GetPatientProfileOutput response from User and Patient entities.
     *
     * @param user the user entity
     * @param patient the patient entity
     * @return GetPatientProfileOutput response
     */
    private GetPatientProfileOutput buildPatientProfileResponse(User user, Patient patient) {
        return GetPatientProfileOutput.builder()
                .build();
    }

}
