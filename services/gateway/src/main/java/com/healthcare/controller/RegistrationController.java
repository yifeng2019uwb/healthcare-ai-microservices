package com.healthcare.controller;

import com.healthcare.api.model.RegisterPatientInput;
import com.healthcare.api.model.RegisterPatientOutput;
import com.healthcare.exception.ConflictException;
import com.healthcare.exception.InternalException;
import com.healthcare.exception.ValidationException;
import com.healthcare.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST Controller for user registration orchestration
 * Handles registration flows that require coordination between multiple services
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * Register a new patient account
     * Orchestrates Supabase Auth + Patient Service calls
     *
     * POST /api/auth/register/patient
     *
     * @param request the patient registration request
     * @return the registration response
     */
    @PostMapping("/register/patient")
    public Mono<ResponseEntity<RegisterPatientOutput>> registerPatient(
            @Valid @RequestBody RegisterPatientInput request) {

        return registrationService.registerPatient(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorReturn(ValidationException.class,
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(RegisterPatientOutput.builder()
                                    .success(false)
                                    .message("Validation error")
                                    .build()))
                .onErrorReturn(ConflictException.class,
                    ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(RegisterPatientOutput.builder()
                                    .success(false)
                                    .message("Account already exists")
                                    .build()))
                .onErrorReturn(InternalException.class,
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(RegisterPatientOutput.builder()
                                    .success(false)
                                    .message("Registration failed")
                                    .build()))
                .onErrorReturn(Exception.class,
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(RegisterPatientOutput.builder()
                                    .success(false)
                                    .message("Unexpected error occurred")
                                    .build()));
    }

    /**
     * Health check endpoint
     * GET /api/auth/health
     *
     * @return health status
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("Gateway Registration Service is healthy"));
    }
}
