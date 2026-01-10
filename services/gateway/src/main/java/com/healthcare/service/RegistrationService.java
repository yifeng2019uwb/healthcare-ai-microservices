package com.healthcare.service;

import com.healthcare.api.model.RegisterPatientInput;
import com.healthcare.api.model.RegisterPatientOutput;
import com.healthcare.api.model.CreatePatientInput;
import com.healthcare.api.model.CreatePatientOutput;
import com.healthcare.client.PatientServiceClient;
import com.healthcare.client.SupabaseAuthClient;
import com.healthcare.exception.ConflictException;
import com.healthcare.exception.InternalException;
import com.healthcare.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for orchestrating user registration across Supabase Auth and business services
 */
@Service
public class RegistrationService {

    @Autowired
    private SupabaseAuthClient supabaseAuthClient;

    @Autowired
    private PatientServiceClient patientServiceClient;

    /**
     * Register a new patient account
     * Orchestrates calls to Supabase Auth and Patient Service
     *
     * @param request the registration request
     * @return the registration response
     */
    public Mono<RegisterPatientOutput> registerPatient(RegisterPatientInput request) {
        return supabaseAuthClient.createUser(request.getEmail(), request.getPassword())
                .flatMap(supabaseResponse -> {
                    // Extract external auth ID from Supabase
                    String externalAuthId = supabaseResponse.getUser().getId();

                    // Create patient account request
                    CreatePatientInput patientRequest = buildPatientAccountRequest(request, externalAuthId);

                    // Call Patient Service
                    return patientServiceClient.createPatientAccount(patientRequest)
                            .map(patientResponse -> {
                                if (patientResponse.success()) {
                                    return RegisterPatientOutput.builder()
                                            .success(true)
                                            .message("Patient account created successfully")
                                            .build();
                                } else {
                                    // If patient creation fails, we should ideally rollback Supabase user
                                    // For MVP, we'll just return the error
                                    throw new InternalException("Failed to create patient profile: " + patientResponse.message());
                                }
                            });
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof ValidationException ||
                        throwable instanceof ConflictException ||
                        throwable instanceof InternalException) {
                        return throwable;
                    }
                    return new InternalException("Registration failed: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Build CreatePatientAccountRequest from registration request and external auth ID
     *
     * @param request the registration request
     * @param externalAuthId the external auth ID from Supabase
     * @return the patient account request
     */
    private CreatePatientInput buildPatientAccountRequest(RegisterPatientInput request, String externalAuthId) {
        return CreatePatientInput.builder()
                .externalUserId(externalAuthId)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .gender(request.gender())
                .build();
    }
}
