package com.healthcare.controller;

import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.LoginResponse;
import com.healthcare.dto.LogoutRequest;
import com.healthcare.dto.RefreshRequest;
import com.healthcare.exception.AuthServiceException;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handles session lifecycle: login, token refresh, and logout.
 *
 * Logout requires both the access token (Authorization header) and
 * the refresh token (request body) so both JTIs can be blacklisted in Redis.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int    BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     *
     * Validates username + password and returns a JWT token pair.
     *
     * @param request login credentials
     * @return 200 with JWT token pair
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     *
     * Validates the refresh token, checks Redis blacklist and absolute session cap,
     * blacklists the old refresh token, and issues a new token pair.
     *
     * @param request refresh token
     * @return 200 with new JWT token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     *
     * Blacklists both the access token JTI and refresh token JTI in Redis.
     * Both tokens are immediately invalidated — no waiting for natural expiry.
     *
     * Requires Authorization: Bearer {access_token} header.
     *
     * @param authHeader  Authorization header containing the access token
     * @param request     request body containing the refresh token
     * @return 200 with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LogoutRequest request) {
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.INVALID_TOKEN,
                    "Authorization header must start with 'Bearer '");
        }
        String accessToken = authHeader.substring(BEARER_PREFIX_LENGTH);
        authService.logout(accessToken, request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}