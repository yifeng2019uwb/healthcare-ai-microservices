package com.healthcare.service;

import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dao.UserDao;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.LoginResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterProviderRequest;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.entity.User;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.AuthServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Claims;

/**
 * Orchestrates all auth-service business logic.
 *
 * Owns: registration, login, token refresh, logout.
 * Delegates to: JwtService (token ops), TokenBlacklistService (Redis),
 *               UserDao/PatientDao/ProviderDao (DB), AuditLogDao (audit).
 *
 * Security principle: internal error details are logged, never returned to callers.
 * All exceptions thrown are AuthServiceException with appropriate HTTP status.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Audit log resource type
    private static final String RESOURCE_USERS = "users";

    // Token type claim value
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final UserDao userDao;
    private final PatientDao patientDao;
    private final ProviderDao providerDao;
    private final AuditLogDao auditLogDao;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDao userDao,
                       PatientDao patientDao,
                       ProviderDao providerDao,
                       AuditLogDao auditLogDao,
                       JwtService jwtService,
                       TokenBlacklistService blacklistService,
                       PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.patientDao = patientDao;
        this.providerDao = providerDao;
        this.auditLogDao = auditLogDao;
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // Registration
    // =========================================================================

    /**
     * Registers a patient account.
     *
     * Flow:
     * 1. Check username + email availability
     * 2. Find patient by MRN
     * 3. Verify first/last name match
     * 4. Check patient not already registered
     * 5. Create User, link to Patient
     * 6. Audit log + issue JWT pair
     */
    @Transactional
    public LoginResponse registerPatient(RegisterPatientRequest request) {
        checkUsernameAndEmailAvailable(request.username(), request.email());

        Patient patient = patientDao.findByMrn(request.mrn())
                .orElseThrow(() -> new AuthServiceException(
                        HttpStatus.NOT_FOUND,
                        AuthServiceException.INVALID_CREDENTIALS,
                        "MRN not found: " + request.mrn()));

        if (!patient.matchesRegistrationCredentials(
                request.mrn(), request.firstName(), request.lastName())) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Registration credentials do not match patient record for MRN: " + request.mrn());
        }

        if (patient.isRegistered()) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "MRN already linked to an account: " + request.mrn());
        }

        User user = createUser(request.username(), request.email(), request.password(), UserRole.PATIENT);
        patient.linkAuthAccount(user.getId());
        patientDao.save(patient);

        auditLogDao.insert(buildAuditLog(user, ActionType.CREATE, Outcome.SUCCESS));
        log.info("Patient registered: username={} mrn={}", request.username(), request.mrn());

        return issueTokenPair(user);
    }

    /**
     * Registers a provider account.
     *
     * Flow:
     * 1. Check username + email availability
     * 2. Find provider by provider_code
     * 3. Verify first/last name match
     * 4. Check provider not already registered
     * 5. Create User, link to Provider
     * 6. Audit log + issue JWT pair
     */
    @Transactional
    public LoginResponse registerProvider(RegisterProviderRequest request) {
        checkUsernameAndEmailAvailable(request.username(), request.email());

        Provider provider = providerDao.findByProviderCode(request.providerCode())
                .orElseThrow(() -> new AuthServiceException(
                        HttpStatus.NOT_FOUND,
                        AuthServiceException.INVALID_CREDENTIALS,
                        "Provider code not found: " + request.providerCode()));

        if (!provider.matchesRegistrationCredentials(
                request.providerCode(), request.firstName() + " " + request.lastName())) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Registration credentials do not match provider record for code: " + request.providerCode());
        }

        if (provider.isRegistered()) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Provider code already linked to an account: " + request.providerCode());
        }

        User user = createUser(request.username(), request.email(), request.password(), UserRole.PROVIDER);
        provider.linkAuthAccount(user.getId());
        providerDao.save(provider);

        auditLogDao.insert(buildAuditLog(user, ActionType.CREATE, Outcome.SUCCESS));
        log.info("Provider registered: username={} providerCode={}",
                request.username(), request.providerCode());

        return issueTokenPair(user);
    }

    // =========================================================================
    // Login
    // =========================================================================

    /**
     * Authenticates a user and issues a JWT token pair.
     *
     * Flow:
     * 1. Find user by username — 401 if not found (don't reveal whether username exists)
     * 2. Verify password — 401 if wrong
     * 3. Check account active — 403 if inactive
     * 4. Audit log + issue JWT pair
     */
    public LoginResponse login(LoginRequest request) {
        User user = userDao.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Login failed — username not found: {}", request.username());
                    return new AuthServiceException(
                            HttpStatus.UNAUTHORIZED,
                            AuthServiceException.INVALID_CREDENTIALS,
                            "Invalid credentials for username: " + request.username());
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed — wrong password for username: {}", request.username());
            auditLogDao.insert(buildAuditLog(user, ActionType.LOGIN, Outcome.FAILURE));
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Invalid credentials for username: " + request.username());
        }

        if (!user.getIsActive()) {
            log.warn("Login failed — account inactive: {}", request.username());
            throw new AuthServiceException(
                    HttpStatus.FORBIDDEN,
                    AuthServiceException.ACCOUNT_INACTIVE,
                    "Account is inactive for username: " + request.username());
        }

        auditLogDao.insert(buildAuditLog(user, ActionType.LOGIN, Outcome.SUCCESS));
        log.info("Login success: username={}", request.username());

        return issueTokenPair(user);
    }

    // =========================================================================
    // Token Refresh
    // =========================================================================

    /**
     * Validates the refresh token, enforces session cap, rotates tokens.
     *
     * Flow:
     * 1. Validate refresh token signature + expiry
     * 2. Verify token type is refresh
     * 3. Check jti not blacklisted
     * 4. Check absolute session cap (8hr from original_iat)
     * 5. Blacklist old refresh token (rotation)
     * 6. Find user → issue new token pair
     */
    public LoginResponse refresh(String refreshToken) {
        Claims claims = jwtService.validateAndExtractClaims(refreshToken);

        if (!TOKEN_TYPE_REFRESH.equals(jwtService.extractTokenType(claims))) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.INVALID_TOKEN,
                    "Token is not a refresh token");
        }

        String jti = jwtService.extractJti(claims);
        if (blacklistService.isBlacklisted(jti)) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.TOKEN_BLACKLISTED,
                    "Refresh token has been revoked jti=" + jti);
        }

        long originalIat = jwtService.extractOriginalIat(claims);
        if (jwtService.isSessionExpired(originalIat)) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.SESSION_EXPIRED,
                    "Absolute session limit exceeded for sub=" + jwtService.extractSubject(claims));
        }

        // Blacklist old refresh token immediately before issuing new ones
        blacklistService.blacklist(claims);

        String userId = jwtService.extractSubject(claims);
        User user = userDao.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new AuthServiceException(
                        HttpStatus.UNAUTHORIZED,
                        AuthServiceException.INVALID_TOKEN,
                        "User not found for sub=" + userId));

        if (!user.getIsActive()) {
            throw new AuthServiceException(
                    HttpStatus.FORBIDDEN,
                    AuthServiceException.ACCOUNT_INACTIVE,
                    "Account is inactive for username=" + user.getUsername());
        }

        log.debug("Token refreshed for username={}", user.getUsername());

        String newAccessToken  = jwtService.issueRotatedAccessToken(user);
        String newRefreshToken = jwtService.issueRotatedRefreshToken(user, originalIat);
        return LoginResponse.of(newAccessToken, newRefreshToken);
    }

    // =========================================================================
    // Logout
    // =========================================================================

    /**
     * Blacklists both access and refresh token JTIs in Redis.
     * Both tokens are immediately invalid regardless of their remaining lifetime.
     *
     * Flow:
     * 1. Validate access token — extract claims
     * 2. Validate refresh token — extract claims
     * 3. Blacklist access token jti
     * 4. Blacklist refresh token jti
     * 5. Audit log
     */
    public void logout(String accessToken, String refreshToken) {
        Claims accessClaims  = jwtService.validateAndExtractClaims(accessToken);
        Claims refreshClaims = jwtService.validateAndExtractClaims(refreshToken);

        blacklistService.blacklist(accessClaims);
        blacklistService.blacklist(refreshClaims);

        String userId = jwtService.extractSubject(accessClaims);
        userDao.findById(java.util.UUID.fromString(userId)).ifPresent(user -> {
            auditLogDao.insert(buildAuditLog(user, ActionType.LOGOUT, Outcome.SUCCESS));
            log.info("Logout success: username={}", user.getUsername());
        });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void checkUsernameAndEmailAvailable(String username, String email) {
        if (userDao.existsByUsername(username)) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Username already exists: " + username);
        }
        if (userDao.existsByEmail(email)) {
            throw new AuthServiceException(
                    HttpStatus.CONFLICT,
                    AuthServiceException.INVALID_CREDENTIALS,
                    "Email already exists: " + email);
        }
    }

    @Transactional
    private User createUser(String username, String email, String password, UserRole role) {
        User user = new User(username, email, passwordEncoder.encode(password), role);
        return userDao.save(user);
    }

    private LoginResponse issueTokenPair(User user) {
        String accessToken  = jwtService.issueAccessToken(user);
        String refreshToken = jwtService.issueRefreshToken(user);
        return LoginResponse.of(accessToken, refreshToken);
    }

    private AuditLog buildAuditLog(User user, ActionType action, Outcome outcome) {
        return new AuditLog(action, RESOURCE_USERS, outcome)
                .withAuthId(user.getId().toString())
                .withUserRole(com.healthcare.enums.UserRole.valueOf(user.getRole().name()))
                .withResourceId(user.getId());
    }
}