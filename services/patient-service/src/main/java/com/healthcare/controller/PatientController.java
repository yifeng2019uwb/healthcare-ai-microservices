package com.healthcare.controller;

import com.healthcare.api.CreatePatientAccountRequest;
import com.healthcare.api.CreatePatientAccountResponse;
import com.healthcare.api.GetPatientProfileRequest;
import com.healthcare.api.GetPatientProfileResponse;
import com.healthcare.entity.User;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        // Build User entity from request
        User user = buildUserFromRequest(request);

        // Call service with User entity
        User savedUser = patientService.createPatient(user);

        // Build success response according to design doc
        CreatePatientAccountResponse response = new CreatePatientAccountResponse(true, "Account created successfully");
        return ResponseEntity.status(201).body(response);

        // TODO: Exception handling - ValidationException (400), BusinessLogicException (409), SystemException (500)
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
        // TODO: Extract userId from JWT token in request
        // For now, we'll need to get userId from somewhere
        // User user = patientService.getUserById(userId);

        // Build response from user
        GetPatientProfileResponse response = buildPatientProfileResponse(null);
        return ResponseEntity.ok(response);
        // TODO: Exception handling - ValidationException (400), ResourceNotFoundException (404), SystemException (500)
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
     * Build patient profile response from user entity.
     *
     * @param user the user entity
     * @return the patient profile response
     */
    private GetPatientProfileResponse buildPatientProfileResponse(User user) {
        // Simple response for now - just return basic user info
        GetPatientProfileResponse response = new GetPatientProfileResponse();
        // TODO: Build proper response with userProfile and patientProfile
        return response;
    }
}
