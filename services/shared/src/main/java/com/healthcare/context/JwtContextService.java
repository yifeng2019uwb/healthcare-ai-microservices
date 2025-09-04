package com.healthcare.context;

/**
 * Service interface for extracting user context from JWT tokens.
 *
 * This interface provides a clean abstraction for extracting user information
 * from JWT tokens across different authentication providers and frameworks.
 *
 * Implementation Examples:
 * - Spring Security: Extract from SecurityContextHolder
 * - Custom JWT: Parse JWT token from request headers
 * - Auth0: Use Auth0 SDK for token validation
 * - Cognito: Use AWS Cognito SDK for token validation
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface JwtContextService {

    /**
     * Extract the current user ID from the JWT context.
     *
     * @return the current user ID, or null if not available
     */
    String getCurrentUserId();

    /**
     * Extract the current user's external auth ID from the JWT context.
     *
     * @return the external auth ID, or null if not available
     */
    String getCurrentExternalAuthId();

    /**
     * Check if the current user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    boolean hasRole(String role);

    /**
     * Check if the current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    boolean isAuthenticated();
}
