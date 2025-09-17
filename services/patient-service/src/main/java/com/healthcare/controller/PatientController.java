package com.healthcare.controller;

import com.healthcare.api.CreatePatientAccountRequest;
import com.healthcare.api.CreatePatientAccountResponse;
import com.healthcare.api.GetPatientProfileRequest;
import com.healthcare.api.GetPatientProfileResponse;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ConflictException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.ValidationException;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Patient operations.
 * Handles patient account creation and profile management.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Create a new patient account.
     * POST /api/patients
     *
     * @param request the patient account creation request
     * @return the creation success response
     */
    @PostMapping
    public ResponseEntity<CreatePatientAccountResponse> createPatient(@RequestBody CreatePatientAccountRequest request) {
        try {
            // Build User entity from request
            User user = buildUserFromRequest(request);

            // Call service with User entity
            patientService.createPatient(user);

            // Build success response
            CreatePatientAccountResponse response = new CreatePatientAccountResponse(true, "Account created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            CreatePatientAccountResponse response = new CreatePatientAccountResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ConflictException e) {
            CreatePatientAccountResponse response = new CreatePatientAccountResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            CreatePatientAccountResponse response = new CreatePatientAccountResponse(false, "System error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get patient profile.
     * GET /api/patients/profile
     *
     * @param request the get patient profile request
     * @return the patient profile
     */
    @GetMapping("/profile")
    public ResponseEntity<GetPatientProfileResponse> getPatientProfile(GetPatientProfileRequest request) {
        try {
            // TODO: Extract userId from JWT token in request
            // For now, we'll use a placeholder - this should be replaced with JWT extraction
            UUID userId = extractUserIdFromRequest(request);

            if (userId == null) {
                GetPatientProfileResponse response = new GetPatientProfileResponse();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Get user and patient data
            User user = patientService.getUserById(userId);
            Patient patient = patientService.getPatientByUserId(userId);

            // Build response from user and patient
            GetPatientProfileResponse response = buildPatientProfileResponse(user, patient);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            GetPatientProfileResponse response = new GetPatientProfileResponse();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            GetPatientProfileResponse response = new GetPatientProfileResponse();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            GetPatientProfileResponse response = new GetPatientProfileResponse();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * Build User entity from create patient request.
     *
     * @param request the create patient request
     * @return the User entity
     */
    private User buildUserFromRequest(CreatePatientAccountRequest request) {
        // Convert gender string to enum (with null check)
        Gender gender = request.getGender() != null ?
            Gender.valueOf(request.getGender().toUpperCase()) :
            Gender.OTHER;

        User user = new User(
            request.getExternalUserId().trim(),
            request.getEmail().trim(),
            UserRole.PATIENT
        );

        // Set optional fields
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(gender);
        return user;
    }

    /**
     * Extract user ID from request (placeholder for JWT extraction).
     * TODO: Replace with proper JWT token extraction
     *
     * @param request the request
     * @return the user ID or null if not found
     */
    private UUID extractUserIdFromRequest(GetPatientProfileRequest request) {
        // TODO: Extract from JWT token in Authorization header
        // For now, return null to trigger error handling
        return null;
    }

    /**
     * Build patient profile response from user and patient entities.
     *
     * @param user the user entity
     * @param patient the patient entity
     * @return the patient profile response
     */
    private GetPatientProfileResponse buildPatientProfileResponse(User user, Patient patient) {
        GetPatientProfileResponse response = new GetPatientProfileResponse();

        // TODO: Build proper response with userProfile and patientProfile
        // This should populate the response DTOs according to the API design

        return response;
    }
}
