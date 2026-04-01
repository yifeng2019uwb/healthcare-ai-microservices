package com.healthcare.service;

import com.healthcare.entity.User;
import com.healthcare.exception.AuthServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * RS256 JWT service — issues and validates access and refresh tokens.
 *
 * JWT payload:
 *   Access token:  sub, username, role, token_type=access, jti, iat, exp
 *   Refresh token: sub, username, role, token_type=refresh, jti, iat, exp, original_iat
 *
 * Expiry:
 *   Access token:  15 minutes (900s)
 *   Refresh token: 1 hour (3600s)
 *   Absolute cap:  8 hours (28800s) enforced via original_iat on refresh
 */
@Service
public class JwtService {

    // Token expiry in milliseconds
    private static final long ACCESS_TOKEN_EXPIRY_MS  = 900_000L;   // 15 min
    private static final long REFRESH_TOKEN_EXPIRY_MS = 3_600_000L; // 1 hour
    private static final long SESSION_MAX_MS           = 28_800_000L; // 8 hours

    // JWT claim names
    private static final String CLAIM_USERNAME     = "username";
    private static final String CLAIM_ROLE         = "role";
    private static final String CLAIM_TOKEN_TYPE   = "token_type";
    private static final String CLAIM_ORIGINAL_IAT = "original_iat";

    // Token type values
    private static final String TYPE_ACCESS  = "access";
    private static final String TYPE_REFRESH = "refresh";

    // PEM parsing
    private static final String PEM_PRIVATE_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_PRIVATE_FOOTER = "-----END PRIVATE KEY-----";
    private static final String PEM_PUBLIC_HEADER  = "-----BEGIN PUBLIC KEY-----";
    private static final String PEM_PUBLIC_FOOTER  = "-----END PUBLIC KEY-----";
    private static final String KEY_ALGORITHM      = "RSA";

    private static final String KEY_ID = "kid";

    @Value("${jwt.private-key}")
    private String privateKeyPem;

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Value("${jwt.key-id}")
    private String keyId;

    // Parsed once at startup — avoids repeated crypto parsing on every token operation
    private RSAPrivateKey cachedPrivateKey;
    private RSAPublicKey cachedPublicKey;

    @PostConstruct
    private void initKeys() {
        this.cachedPrivateKey = parsePrivateKey();
        this.cachedPublicKey  = parsePublicKey();
    }



    // -------------------------------------------------------------------------
    // Token issuance
    // -------------------------------------------------------------------------

    /**
     * Issues an access token for the given user.
     * original_iat is set to now — use only on fresh login or registration.
     */
    public String issueAccessToken(User user) {
        long now = System.currentTimeMillis();
        return buildToken(user, TYPE_ACCESS, now, ACCESS_TOKEN_EXPIRY_MS, now);
    }

    /**
     * Issues a refresh token for the given user.
     * original_iat is set to now — use only on fresh login or registration.
     */
    public String issueRefreshToken(User user) {
        long now = System.currentTimeMillis();
        return buildToken(user, TYPE_REFRESH, now, REFRESH_TOKEN_EXPIRY_MS, now);
    }

    /**
     * Issues a new refresh token carrying forward the original_iat from the old one.
     * Used during token rotation — preserves the 8-hour absolute session cap.
     *
     * @param user        the authenticated user
     * @param originalIat original login timestamp in epoch milliseconds
     */
    public String issueRotatedRefreshToken(User user, long originalIat) {
        long now = System.currentTimeMillis();
        return buildToken(user, TYPE_REFRESH, now, REFRESH_TOKEN_EXPIRY_MS, originalIat);
    }

    /**
     * Issues a new access token during refresh — does not reset original_iat.
     */
    public String issueRotatedAccessToken(User user) {
        return issueAccessToken(user);
    }

    // -------------------------------------------------------------------------
    // Token validation
    // -------------------------------------------------------------------------

    /**
     * Parses and validates a token. Throws AuthServiceException on any failure.
     *
     * @param token raw JWT string
     * @return parsed claims
     * @throws AuthServiceException INVALID_TOKEN if malformed or wrong signature
     * @throws AuthServiceException TOKEN_EXPIRED if past expiry
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(cachedPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.TOKEN_EXPIRED,
                    "Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    AuthServiceException.INVALID_TOKEN,
                    "Invalid token: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Claims extraction — called after validateAndExtractClaims
    // -------------------------------------------------------------------------

    public String extractJti(Claims claims) {
        return claims.getId();
    }

    public String extractUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public String extractRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    public String extractTokenType(Claims claims) {
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    public String extractSubject(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extracts original_iat in epoch milliseconds.
     * Used to enforce the 8-hour absolute session cap on refresh.
     */
    public long extractOriginalIat(Claims claims) {
        return claims.get(CLAIM_ORIGINAL_IAT, Long.class);
    }

    /**
     * Returns remaining token lifetime in seconds — used to set Redis blacklist TTL.
     */
    public long getRemainingTtlSeconds(Claims claims) {
        long expMs = claims.getExpiration().getTime();
        long remainingMs = expMs - System.currentTimeMillis();
        return Math.max(0, remainingMs / 1000);
    }

    // -------------------------------------------------------------------------
    // Session cap check
    // -------------------------------------------------------------------------

    /**
     * Returns true if the absolute 8-hour session cap has been exceeded.
     *
     * @param originalIat original login timestamp in epoch milliseconds
     */
    public boolean isSessionExpired(long originalIat) {
        return System.currentTimeMillis() - originalIat > SESSION_MAX_MS;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildToken(User user, String tokenType, long nowMs,
                              long expiryMs, long originalIat) {
        Date issuedAt  = new Date(nowMs);
        Date expiresAt = new Date(nowMs + expiryMs);

        return Jwts.builder()
                .header().add(KEY_ID, keyId).and()
                .subject(user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .claim(CLAIM_ORIGINAL_IAT, originalIat)
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(cachedPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    private RSAPrivateKey parsePrivateKey() {
        try {
            String stripped = privateKeyPem
                    .replace(PEM_PRIVATE_HEADER, "")
                    .replace(PEM_PRIVATE_FOOTER, "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(stripped);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new AuthServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthServiceException.INTERNAL_ERROR,
                    "Failed to parse RS256 private key", e);
        }
    }

    private RSAPublicKey parsePublicKey() {
        try {
            String stripped = publicKeyPem
                    .replace(PEM_PUBLIC_HEADER, "")
                    .replace(PEM_PUBLIC_FOOTER, "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(stripped);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new AuthServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthServiceException.INTERNAL_ERROR,
                    "Failed to parse RS256 public key", e);
        }
    }
}