package com.healthcare.service.impl;

import com.healthcare.dao.PatientDao;
import com.healthcare.dao.UserDao;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.enums.UserStatus;
import com.healthcare.exception.ConflictException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.ValidationException;
import com.healthcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service implementation for Patient operations.
 * Handles patient account creation and profile management.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PatientDao patientDao;

    @Override
    public User createPatient(User user) {
        // Validate input
        validateUserForPatientCreation(user);

        // Check if user already exists (conflict - resource already exists)
        if (userDao.findByExternalAuthId(user.getExternalAuthId()).isPresent()) {
            throw new ConflictException("User with external ID already exists: " + user.getExternalAuthId());
        }

        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("User with email already exists: " + user.getEmail());
        }

        // Set user status (role is already set in constructor)
        user.setStatus(UserStatus.ACTIVE);

        // Save user
        User savedUser = userDao.create(user);

        // Create patient profile
        Patient patient = new Patient(savedUser.getId(), generatePatientNumber());

        // Save patient
        patientDao.create(patient);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        return userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get patient by user ID.
     *
     * @param userId the user ID
     * @return the patient entity
     */
    @Transactional(readOnly = true)
    public Patient getPatientByUserId(UUID userId) {
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        return patientDao.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user ID: " + userId));
    }

    /**
     * Get patient by patient number.
     *
     * @param patientNumber the patient number
     * @return the patient entity
     */
    @Transactional(readOnly = true)
    public Patient getPatientByNumber(String patientNumber) {
        if (patientNumber == null || patientNumber.trim().isEmpty()) {
            throw new ValidationException("Patient number cannot be null or empty");
        }

        return patientDao.findByPatientNumber(patientNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with number: " + patientNumber));
    }

    /**
     * Update patient information.
     *
     * @param patient the patient entity to update
     * @return the updated patient entity
     */
    public Patient updatePatient(Patient patient) {
        if (patient == null) {
            throw new ValidationException("Patient cannot be null");
        }

        if (patient.getId() == null) {
            throw new ValidationException("Patient ID cannot be null for update");
        }

        // Check if patient exists
        if (!patientDao.existsById(patient.getId())) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patient.getId());
        }

        return patientDao.update(patient);
    }

    /**
     * Validate user for patient creation.
     *
     * @param user the user entity to validate
     */
    private void validateUserForPatientCreation(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }

        if (user.getExternalAuthId() == null || user.getExternalAuthId().trim().isEmpty()) {
            throw new ValidationException("External authentication ID is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }

        // Validate email format using simple regex
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!user.getEmail().matches(emailPattern)) {
            throw new ValidationException("Invalid email format: " + user.getEmail());
        }

        // Validate date of birth if provided
        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future");
        }
    }

    /**
     * Generate a unique patient number.
     *
     * @return the generated patient number
     */
    private String generatePatientNumber() {
        String prefix = "PAT-";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomSuffix = String.valueOf((int) (Math.random() * 1000));
        return prefix + timestamp.substring(timestamp.length() - 8) + randomSuffix;
    }
}