package com.healthcare.dao;

import com.healthcare.entity.User;
import com.healthcare.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for User entity
 * Handles database operations for user profiles
 */
@Repository
public interface UserDao extends JpaRepository<User, UUID> {

    /**
     * Find user by external authentication ID
     * Used for authentication and user lookup
     *
     * @param externalAuthId The external authentication provider ID
     * @return Optional containing the user if found
     */
    Optional<User> findByExternalAuthId(String externalAuthId);

    /**
     * Find user by email address
     * Used for login and registration validation
     *
     * @param email The user's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Create a new user
     * Saves the user entity to the database
     *
     * @param user The user entity to create
     * @return The created user entity
     */
    default User create(User user) {
        return save(user);
    }

    /**
     * Update an existing user
     * Updates the user entity in the database
     *
     * @param user The user entity to update
     * @return The updated user entity
     */
    default User update(User user) {
        return save(user);
    }
}
