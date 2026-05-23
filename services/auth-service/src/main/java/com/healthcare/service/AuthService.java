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

import java.time.LocalDate;
import java.util.List;
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
 * Delegates to: JwtService (token ops),
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
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDao userDao,
                       PatientDao patientDao,
                       ProviderDao providerDao,
                       AuditLogDao auditLogDao,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.patientDao = patientDao;
        this.providerDao = providerDao;
        this.auditLogDao = auditLogDao;
        this.jwtService = jwtService;
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
     * 2. Validate patient record exists and is unlinked (0 → 422, 2+ → 422, already linked → 409)
     * 3. Create User, link to Patient, audit + issue JWT pair
     */
    @Transactional
    public LoginResponse registerPatient(RegisterPatientRequest request) {
        log.info("Register patient attempt: username={}, firstName={}, lastName={}, dob={}",
                request.username(), request.firstName(), request.lastName(), request.birthdate());
        checkUsernameAndEmailAvailable(request.username(), request.email());

        Patient patient = findUniquePatient(request.firstName(), request.lastName(), request.birthdate());
        User user = createUser(request.username(), request.email(), request.password(), UserRole.PATIENT);
        patient.linkAuthAccount(user.getId());
        patientDao.save(patient);

        auditLogDao.insert(buildAuditLog(user, ActionType.CREATE, Outcome.SUCCESS));
        log.info("Patient registered: username={}", request.username());

        return issueTokenPair(user);
    }

    /**
     * Registers a provider account.
     *
     * Flow:
     * 1. Check username + email availability
     * 2. Validate provider record exists and is unlinked (0 → 422, 2+ → 422, already linked → 409)
     * 3. Create User, link to Provider, audit + issue JWT pair
     */
    @Transactional
    public LoginResponse registerProvider(RegisterProviderRequest request) {
        log.info("Register provider attempt: username={}, name={}", request.username(), request.name());
        checkUsernameAndEmailAvailable(request.username(), request.email());

        Provider provider = findUniqueProvider(request.name());
        User user = createUser(request.username(), request.email(), request.password(), UserRole.PROVIDER);
        provider.linkAuthAccount(user.getId());
        providerDao.save(provider);

        auditLogDao.insert(buildAuditLog(user, ActionType.CREATE, Outcome.SUCCESS));
        log.info("Provider registered: username={}", request.username());

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
     * 3. Check absolute session cap (8hr from original_iat)
     * 4. Find user → issue new token pair
     */
    public LoginResponse refresh(String refreshToken) {
        Claims claims = jwtService.validateAndExtractClaims(refreshToken);

        if (!TOKEN_TYPE_REFRESH.equals(jwtService.extractTokenType(claims))) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.INVALID_TOKEN,
                    "Token is not a refresh token");
        }

        long originalIat = jwtService.extractOriginalIat(claims);
        if (jwtService.isSessionExpired(originalIat)) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.SESSION_EXPIRED,
                    "Absolute session limit exceeded for sub=" + jwtService.extractSubject(claims));
        }

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
     * Validates both tokens and records an audit log entry.
     * Tokens expire naturally via JWT TTL.
     *
     * Flow:
     * 1. Validate access token — extract claims
     * 2. Validate refresh token
     * 3. Audit log
     */
    public void logout(String accessToken, String refreshToken) {
        Claims accessClaims = jwtService.validateAndExtractClaims(accessToken);
        jwtService.validateAndExtractClaims(refreshToken);

        String userId = jwtService.extractSubject(accessClaims);
        userDao.findById(java.util.UUID.fromString(userId)).ifPresent(user -> {
            auditLogDao.insert(buildAuditLog(user, ActionType.LOGOUT, Outcome.SUCCESS));
            log.info("Logout success: username={}", user.getUsername());
        });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Patient findUniquePatient(String firstName, String lastName, LocalDate birthdate) {
        List<Patient> matches =
                patientDao.findByFirstNameAndLastNameAndBirthdate(firstName, lastName, birthdate);
        if (matches.isEmpty()) {
            throw new AuthServiceException(HttpStatus.UNPROCESSABLE_ENTITY,
                    AuthServiceException.RECORD_NOT_FOUND,
                    "No matching patient record found for: " + firstName + " " + lastName);
        }
        if (matches.size() > 1) {
            throw new AuthServiceException(HttpStatus.UNPROCESSABLE_ENTITY,
                    AuthServiceException.MULTIPLE_RECORDS_FOUND,
                    "Multiple patient records match — contact support to resolve");
        }
        Patient patient = matches.get(0);
        if (patient.isRegistered()) {
            throw new AuthServiceException(HttpStatus.CONFLICT,
                    AuthServiceException.ALREADY_REGISTERED,
                    "An account is already linked to this patient record");
        }
        return patient;
    }

    // TODO: production registration should also validate organization name + NPI/license to
    //  narrow the match; name-only is sufficient for Synthea data where duplicates are rare.
    private Provider findUniqueProvider(String name) {
        List<Provider> matches = providerDao.findByName(name);
        if (matches.isEmpty()) {
            throw new AuthServiceException(HttpStatus.UNPROCESSABLE_ENTITY,
                    AuthServiceException.RECORD_NOT_FOUND,
                    "No matching provider record found for: " + name);
        }
        if (matches.size() > 1) {
            throw new AuthServiceException(HttpStatus.UNPROCESSABLE_ENTITY,
                    AuthServiceException.MULTIPLE_RECORDS_FOUND,
                    "Multiple provider records match — contact support to resolve");
        }
        Provider provider = matches.get(0);
        if (provider.isRegistered()) {
            throw new AuthServiceException(HttpStatus.CONFLICT,
                    AuthServiceException.ALREADY_REGISTERED,
                    "An account is already linked to this provider record");
        }
        return provider;
    }

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