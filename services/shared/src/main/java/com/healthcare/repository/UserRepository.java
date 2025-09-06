package com.healthcare.repository;

import com.healthcare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

/**
 * Repository interface for User entity
 *
 * Provides basic CRUD operations and custom query methods
 * for User entity database operations.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     * Future use: Authentication, user lookup
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     * Future use: Phone-based authentication, user lookup
     *
     * @param phone the phone number
     * @return Optional containing the user if found
     */
    Optional<User> findByPhone(String phone);

    /**
     * Find user by external auth ID
     * Used by: Authentication, user lookup
     *
     * @param externalAuthId the external authentication ID
     * @return Optional containing the user if found
     */
    Optional<User> findByExternalAuthId(String externalAuthId);

    // ==================== FUTURE METHODS ====================
    // TODO: Add these methods when needed

    /**
     * Find user by name and date of birth
     * Future use: Patient lookup, identity verification
     */
    // Optional<User> findByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, LocalDate dateOfBirth);

    /**
     * Find users by status
     * Future use: Filtering, reporting
     */
    // List<User> findByStatus(UserStatus status);

    /**
     * Find users by role
     * Future use: Role-based operations, filtering
     */
    // List<User> findByRole(UserRole role);

    /**
     * Check if email exists
     * Future use: Registration validation
     */
    // boolean existsByEmail(String email);

    /**
     * Check if phone exists
     * Future use: Registration validation
     */
    // boolean existsByPhone(String phone);
}
