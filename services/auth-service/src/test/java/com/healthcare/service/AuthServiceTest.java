package com.healthcare.service;

import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dao.UserDao;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.LoginResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterProviderRequest;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.entity.User;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.AuthServiceException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService} — happy path only.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserDao userDao;
    @Mock private PatientDao patientDao;
    @Mock private ProviderDao providerDao;
    @Mock private AuditLogDao auditLogDao;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
    }

    /** Stubs the fields read by buildAuditLog — call in tests that trigger an audit entry. */
    private void stubUserForAuditLog() {
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(UserRole.PATIENT);
    }

    // =========================================================================
    // Login
    // =========================================================================

    @Test
    void login_happyPath_returnsTokenPair() {
        stubUserForAuditLog();
        when(mockUser.getPasswordHash()).thenReturn("$2a$encoded");
        when(mockUser.getIsActive()).thenReturn(true);
        when(userDao.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);
        when(jwtService.issueAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.issueRefreshToken(mockUser)).thenReturn("refresh-token");

        LoginResponse response = authService.login(new LoginRequest("john_doe", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(auditLogDao).insert(any());
    }

    // =========================================================================
    // Register patient
    // =========================================================================

    @Test
    void registerPatient_happyPath_returnsTokenPair() {
        stubUserForAuditLog();
        Patient mockPatient = mock(Patient.class);
        when(mockPatient.matchesRegistrationCredentials("MRN001", "John", "Doe")).thenReturn(true);
        when(mockPatient.isRegistered()).thenReturn(false);

        when(userDao.existsByUsername("john_doe")).thenReturn(false);
        when(userDao.existsByEmail("john@example.com")).thenReturn(false);
        when(patientDao.findByMrn("MRN001")).thenReturn(Optional.of(mockPatient));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(userDao.save(any())).thenReturn(mockUser);
        when(patientDao.save(mockPatient)).thenReturn(mockPatient);
        when(jwtService.issueAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.issueRefreshToken(mockUser)).thenReturn("refresh-token");

        RegisterPatientRequest request = new RegisterPatientRequest(
                "john_doe", "john@example.com", "Password1@", "MRN001", "John", "Doe");

        LoginResponse response = authService.registerPatient(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(mockPatient).linkAuthAccount(userId);
        verify(auditLogDao).insert(any());
    }

    // =========================================================================
    // Register provider
    // =========================================================================

    @Test
    void registerProvider_happyPath_returnsTokenPair() {
        Provider mockProvider = mock(Provider.class);
        when(mockProvider.matchesRegistrationCredentials("PRV-000001", "Jane Doe")).thenReturn(true);
        when(mockProvider.isRegistered()).thenReturn(false);

        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(UserRole.PROVIDER);
        when(userDao.existsByUsername("jane_doe")).thenReturn(false);
        when(userDao.existsByEmail("jane@example.com")).thenReturn(false);
        when(providerDao.findByProviderCode("PRV-000001")).thenReturn(Optional.of(mockProvider));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(userDao.save(any())).thenReturn(mockUser);
        when(providerDao.save(mockProvider)).thenReturn(mockProvider);
        when(jwtService.issueAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.issueRefreshToken(mockUser)).thenReturn("refresh-token");

        RegisterProviderRequest request = new RegisterProviderRequest(
                "jane_doe", "jane@example.com", "Password1@", "PRV-000001", "Jane", "Doe");

        LoginResponse response = authService.registerProvider(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(mockProvider).linkAuthAccount(userId);
        verify(auditLogDao).insert(any());
    }

    // =========================================================================
    // Refresh
    // =========================================================================

    @Test
    void refresh_happyPath_returnsNewTokenPair() {
        Claims mockClaims = mock(Claims.class);
        long originalIat = System.currentTimeMillis() - 60_000;

        when(jwtService.validateAndExtractClaims("old-refresh-token")).thenReturn(mockClaims);
        when(jwtService.extractTokenType(mockClaims)).thenReturn("refresh");
        when(jwtService.extractOriginalIat(mockClaims)).thenReturn(originalIat);
        when(jwtService.isSessionExpired(originalIat)).thenReturn(false);
        when(jwtService.extractSubject(mockClaims)).thenReturn(userId.toString());
        when(userDao.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getIsActive()).thenReturn(true);
        when(mockUser.getUsername()).thenReturn("john_doe");
        when(jwtService.issueRotatedAccessToken(mockUser)).thenReturn("new-access-token");
        when(jwtService.issueRotatedRefreshToken(eq(mockUser), anyLong())).thenReturn("new-refresh-token");

        LoginResponse response = authService.refresh("old-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    // =========================================================================
    // Logout
    // =========================================================================

    @Test
    void logout_happyPath_auditsLogout() {
        stubUserForAuditLog();
        Claims accessClaims  = mock(Claims.class);
        Claims refreshClaims = mock(Claims.class);

        when(jwtService.validateAndExtractClaims("access-token")).thenReturn(accessClaims);
        when(jwtService.validateAndExtractClaims("refresh-token")).thenReturn(refreshClaims);
        when(jwtService.extractSubject(accessClaims)).thenReturn(userId.toString());
        when(userDao.findById(userId)).thenReturn(Optional.of(mockUser));

        authService.logout("access-token", "refresh-token");

        verify(auditLogDao).insert(any());
    }

    // =========================================================================
    // Refresh — error paths
    // =========================================================================

    @Test
    void refresh_withNonRefreshToken_throws401() {
        Claims mockClaims = mock(Claims.class);
        when(jwtService.validateAndExtractClaims("access-token")).thenReturn(mockClaims);
        when(jwtService.extractTokenType(mockClaims)).thenReturn("access");

        assertThatThrownBy(() -> authService.refresh("access-token"))
                .isInstanceOfSatisfying(AuthServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getErrorCode()).isEqualTo(AuthServiceException.INVALID_TOKEN);
                });
    }

    @Test
    void refresh_withExpiredSession_throws401() {
        Claims mockClaims = mock(Claims.class);
        long oldIat = System.currentTimeMillis() - 9 * 60 * 60 * 1000L;

        when(jwtService.validateAndExtractClaims("expired-token")).thenReturn(mockClaims);
        when(jwtService.extractTokenType(mockClaims)).thenReturn("refresh");
        when(jwtService.extractOriginalIat(mockClaims)).thenReturn(oldIat);
        when(jwtService.isSessionExpired(oldIat)).thenReturn(true);
        when(jwtService.extractSubject(mockClaims)).thenReturn(userId.toString());

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOfSatisfying(AuthServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getErrorCode()).isEqualTo(AuthServiceException.SESSION_EXPIRED);
                });
    }

    @Test
    void refresh_withUnknownUser_throws401() {
        Claims mockClaims = mock(Claims.class);
        long recentIat = System.currentTimeMillis() - 60_000;

        when(jwtService.validateAndExtractClaims("unknown-token")).thenReturn(mockClaims);
        when(jwtService.extractTokenType(mockClaims)).thenReturn("refresh");
        when(jwtService.extractOriginalIat(mockClaims)).thenReturn(recentIat);
        when(jwtService.isSessionExpired(recentIat)).thenReturn(false);
        when(jwtService.extractSubject(mockClaims)).thenReturn(userId.toString());
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOfSatisfying(AuthServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getErrorCode()).isEqualTo(AuthServiceException.INVALID_TOKEN);
                });
    }

    @Test
    void refresh_withInactiveUser_throws403() {
        Claims mockClaims = mock(Claims.class);
        long recentIat = System.currentTimeMillis() - 60_000;

        when(jwtService.validateAndExtractClaims("inactive-token")).thenReturn(mockClaims);
        when(jwtService.extractTokenType(mockClaims)).thenReturn("refresh");
        when(jwtService.extractOriginalIat(mockClaims)).thenReturn(recentIat);
        when(jwtService.isSessionExpired(recentIat)).thenReturn(false);
        when(jwtService.extractSubject(mockClaims)).thenReturn(userId.toString());
        when(userDao.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getIsActive()).thenReturn(false);
        when(mockUser.getUsername()).thenReturn("john_doe");

        assertThatThrownBy(() -> authService.refresh("inactive-token"))
                .isInstanceOfSatisfying(AuthServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getErrorCode()).isEqualTo(AuthServiceException.ACCOUNT_INACTIVE);
                });
    }

    // =========================================================================
    // Registration — fhirId wiring
    // =========================================================================

    @Test
    void registerPatient_setsFhirIdFromPatient() {
        UUID patientFhirId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        stubUserForAuditLog();
        Patient mockPatient = mock(Patient.class);
        when(mockPatient.matchesRegistrationCredentials("MRN001", "John", "Doe")).thenReturn(true);
        when(mockPatient.isRegistered()).thenReturn(false);
        when(mockPatient.getId()).thenReturn(patientFhirId);

        when(userDao.existsByUsername("john_doe")).thenReturn(false);
        when(userDao.existsByEmail("john@example.com")).thenReturn(false);
        when(patientDao.findByMrn("MRN001")).thenReturn(Optional.of(mockPatient));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(userDao.save(any())).thenReturn(mockUser);
        when(patientDao.save(mockPatient)).thenReturn(mockPatient);
        when(jwtService.issueAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.issueRefreshToken(mockUser)).thenReturn("refresh-token");

        authService.registerPatient(new RegisterPatientRequest(
                "john_doe", "john@example.com", "Password1@", "MRN001", "John", "Doe"));

        verify(mockUser).setFhirId(patientFhirId);
    }

    @Test
    void registerProvider_setsFhirIdFromProvider() {
        UUID providerFhirId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(UserRole.PROVIDER);
        Provider mockProvider = mock(Provider.class);
        when(mockProvider.matchesRegistrationCredentials("PRV-000001", "Jane Doe")).thenReturn(true);
        when(mockProvider.isRegistered()).thenReturn(false);
        when(mockProvider.getId()).thenReturn(providerFhirId);

        when(userDao.existsByUsername("jane_doe")).thenReturn(false);
        when(userDao.existsByEmail("jane@example.com")).thenReturn(false);
        when(providerDao.findByProviderCode("PRV-000001")).thenReturn(Optional.of(mockProvider));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(userDao.save(any())).thenReturn(mockUser);
        when(providerDao.save(mockProvider)).thenReturn(mockProvider);
        when(jwtService.issueAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.issueRefreshToken(mockUser)).thenReturn("refresh-token");

        authService.registerProvider(new RegisterProviderRequest(
                "jane_doe", "jane@example.com", "Password1@", "PRV-000001", "Jane", "Doe"));

        verify(mockUser).setFhirId(providerFhirId);
    }
}
