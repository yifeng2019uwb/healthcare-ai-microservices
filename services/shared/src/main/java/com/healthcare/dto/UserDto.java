package com.healthcare.dto;

import com.healthcare.entity.User;
import com.healthcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;

/**
 * DTO for User entity CRUD operations
 *
 * Handles database operations for User entity with focus on
 * Patient Service API requirements.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Component
public class UserDto {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user
     * Used by: POST /api/patients (Create Patient Account)
     *
     * @param user the user entity to create
     * @return the saved user entity
     */
    public User createUser(User user) {
        // Database generates UUID via gen_random_uuid() DEFAULT
        // Entity ID will be null until after save() operation
        return userRepository.save(user);
    }

    /**
     * Update user entity
     * Used by: PUT /api/patients/profile (Update Personal Profile)
     *
     * @param user the user entity to update
     * @return the updated user entity
     */
    public User updateUser(User user) {
        // For updates, user must exist and have an ID
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID is required for update");
        }

        // Check if user exists
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " does not exist");
        }

        return userRepository.save(user);
    }

    /**
     * Get user by ID
     * Used by: GET /api/patients/profile (Get Patient Profile)
     *
     * @param userId the user ID
     * @return the user entity or null if not found
     */
    public User getUserById(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    // ==================== FUTURE METHODS ====================
    // TODO: Implement these methods when needed

    /**
     * Get user by email address
     * Future use: Authentication, user lookup
     */
    // public User getUserByEmail(String email) {
    //     return userRepository.findByEmail(email).orElse(null);
    // }

    /**
     * Get user by phone number
     * Future use: Phone-based authentication, user lookup
     */
    // public User getUserByPhone(String phone) {
    //     return userRepository.findByPhone(phone).orElse(null);
    // }

    /**
     * Get user by name and date of birth
     * Future use: Patient lookup, identity verification
     */
    // public User getUserByNameAndDob(String firstName, String lastName, LocalDate dateOfBirth) {
    //     return userRepository.findByFirstNameAndLastNameAndDateOfBirth(firstName, lastName, dateOfBirth).orElse(null);
    // }

    /**
     * Get all users
     * Future use: Admin operations, reporting
     */
    // public List<User> getAllUsers() {
    //     return userRepository.findAll();
    // }

    /**
     * Get users by status
     * Future use: Filtering, reporting
     */
    // public List<User> getUsersByStatus(UserStatus status) {
    //     return userRepository.findByStatus(status);
    // }

    /**
     * Get users by role
     * Future use: Role-based operations, filtering
     */
    // public List<User> getUsersByRole(UserRole role) {
    //     return userRepository.findByRole(role);
    // }

    /**
     * Delete user by ID
     * Future use: User management, data cleanup
     */
    // public void deleteUser(UUID userId) {
    //     // TODO: Soft delete or hard delete based on business rules
    //     userRepository.deleteById(userId);
    // }
}
