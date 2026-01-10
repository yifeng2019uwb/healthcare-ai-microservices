package com.healthcare.controller;

import com.healthcare.api.model.CreatePatientInput;
import com.healthcare.api.model.CreatePatientOutput;
import com.healthcare.constants.PatientServiceConstants;
import com.healthcare.entity.User;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ConflictException;
import com.healthcare.exception.ValidationException;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Create Patient operations.
 * Handles patient account creation.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping(PatientServiceConstants.CREATE_PATIENT_ENDPOINT)
@CrossOrigin(origins = "*")
public class CreatePatientController {

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
    public ResponseEntity<CreatePatientOutput> createPatient(@RequestBody CreatePatientInput request) {
        try {
            // Build User entity from request
            User user = buildUserFromRequest(request);

            // Call service with User entity
            patientService.createPatient(user);

            // Build success response
            CreatePatientOutput response = CreatePatientOutput.builder()
                    .success(true)
                    .message("Account created successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            CreatePatientOutput response = CreatePatientOutput.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ConflictException e) {
            CreatePatientOutput response = CreatePatientOutput.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            CreatePatientOutput response = CreatePatientOutput.builder()
                    .success(false)
                    .message("System error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Build User entity from CreatePatientInput request.
     *
     * @param request the request object
     * @return User entity
     */
    private User buildUserFromRequest(CreatePatientInput request) {
        // Use the public constructor for required fields
        User user = new User(
            request.externalUserId(),
            request.email(),
            UserRole.PATIENT
        );

        // Set optional fields using setters
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setGender(Gender.OTHER); // Default value, should be provided in request
        return user;
    }
}
