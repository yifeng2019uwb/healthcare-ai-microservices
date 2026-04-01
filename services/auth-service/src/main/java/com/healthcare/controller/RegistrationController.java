package com.healthcare.controller;

import com.healthcare.dto.LoginResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterProviderRequest;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles patient and provider account registration.
 *
 * Registration validates credentials against pre-existing records:
 * - Patient:  MRN + first_name + last_name must match patients table
 * - Provider: provider_code + first_name + last_name must match providers table
 *
 * On success, creates a users record, links auth_id to the patient/provider,
 * and returns a JWT token pair (auto-login after registration).
 */
@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final AuthService authService;

    public RegistrationController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register/patient
     *
     * Creates a patient account and links it to the existing patient record via MRN.
     *
     * @param request validated registration fields
     * @return 201 with JWT token pair on success
     */
    @PostMapping("/register/patient")
    public ResponseEntity<LoginResponse> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {
        LoginResponse response = authService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/register/provider
     *
     * Creates a provider account and links it to the existing provider record via provider_code.
     *
     * @param request validated registration fields
     * @return 201 with JWT token pair on success
     */
    @PostMapping("/register/provider")
    public ResponseEntity<LoginResponse> registerProvider(
            @Valid @RequestBody RegisterProviderRequest request) {
        LoginResponse response = authService.registerProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}