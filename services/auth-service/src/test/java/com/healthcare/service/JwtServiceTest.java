package com.healthcare.service;

import com.healthcare.entity.User;
import com.healthcare.enums.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtService} — happy path only.
 * Uses a generated RSA key pair injected via ReflectionTestUtils.
 */
class JwtServiceTest {

    private static String testPrivatePem;
    private static String testPublicPem;

    private JwtService jwtService;
    private User mockUser;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        Base64.Encoder enc = Base64.getEncoder();
        testPrivatePem = "-----BEGIN PRIVATE KEY-----\n"
                + enc.encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";
        testPublicPem = "-----BEGIN PUBLIC KEY-----\n"
                + enc.encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "privateKeyPem", testPrivatePem);
        ReflectionTestUtils.setField(jwtService, "publicKeyPem", testPublicPem);
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-v1");
        ReflectionTestUtils.invokeMethod(jwtService, "initKeys");

        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(mockUser.getUsername()).thenReturn("john_doe");
        when(mockUser.getRole()).thenReturn(UserRole.PATIENT);
    }

    @Test
    void issueAccessToken_producesValidTokenWithCorrectClaims() {
        String token = jwtService.issueAccessToken(mockUser);

        Claims claims = jwtService.validateAndExtractClaims(token);
        assertThat(claims.getSubject()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(jwtService.extractUsername(claims)).isEqualTo("john_doe");
        assertThat(jwtService.extractRole(claims)).isEqualTo("PATIENT");
        assertThat(jwtService.extractTokenType(claims)).isEqualTo("access");
        assertThat(jwtService.extractJti(claims)).isNotBlank();
    }

    @Test
    void issueRefreshToken_producesTokenWithRefreshType() {
        String token = jwtService.issueRefreshToken(mockUser);

        Claims claims = jwtService.validateAndExtractClaims(token);
        assertThat(jwtService.extractTokenType(claims)).isEqualTo("refresh");
    }

    @Test
    void issueRotatedRefreshToken_preservesOriginalIat() {
        long originalIat = System.currentTimeMillis() - 60_000;

        String token = jwtService.issueRotatedRefreshToken(mockUser, originalIat);

        Claims claims = jwtService.validateAndExtractClaims(token);
        assertThat(jwtService.extractOriginalIat(claims)).isEqualTo(originalIat);
        assertThat(jwtService.extractTokenType(claims)).isEqualTo("refresh");
    }

    @Test
    void getRemainingTtlSeconds_isPositiveForFreshToken() {
        String token = jwtService.issueAccessToken(mockUser);
        Claims claims = jwtService.validateAndExtractClaims(token);

        long ttl = jwtService.getRemainingTtlSeconds(claims);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(900);
    }

    @Test
    void isSessionExpired_returnsFalseForRecentLogin() {
        long recentIat = System.currentTimeMillis() - 60_000; // 1 minute ago
        assertThat(jwtService.isSessionExpired(recentIat)).isFalse();
    }
}
