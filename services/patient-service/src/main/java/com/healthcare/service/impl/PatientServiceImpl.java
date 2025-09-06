package com.healthcare.service.impl;

import com.healthcare.dto.PatientDto;
import com.healthcare.dto.UserDto;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation for Patient operations.
 * Handles business logic for patient management.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private UserDto userDto;

    @Autowired
    private PatientDto patientDto;

    @Override
    public User createPatient(User user) {
        try {
            // Save User to database
            User savedUser = userDto.createUser(user);
            System.out.println("✅ User created with ID: " + savedUser.getId());

            // Create Patient entity
            String patientNumber = "P" + System.currentTimeMillis(); // Simple patient number
            Patient patient = new Patient(savedUser.getId(), patientNumber);

            // Save Patient to database
            Patient savedPatient = patientDto.createPatient(patient);
            System.out.println("✅ Patient created with ID: " + savedPatient.getId());

            // Return the saved user
            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ Error creating patient: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create patient", e);
        }
    }

    @Override
    public User getUserById(UUID userId) {
        try {
            // Get User from database
            User user = userDto.getUserById(userId);
            if (user == null) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            System.out.println("✅ User retrieved with ID: " + userId);
            return user;

        } catch (Exception e) {
            System.err.println("❌ Error getting user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get user", e);
        }
    }
}
