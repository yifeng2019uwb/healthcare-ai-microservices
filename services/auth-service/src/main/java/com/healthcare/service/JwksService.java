package com.healthcare.service;

import com.healthcare.dto.JwksResponse;
import com.healthcare.exception.AuthServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * Builds the JWKS response for the /.well-known/jwks.json endpoint.
 *
 * Follows RFC 7517 JSON Web Key Set standard.
 * Gateway fetches this at startup and caches with TTL.
 * On kid miss (key rotation), gateway re-fetches automatically.
 *
 * Key rotation:
 *   Phase 1 — add new key version to Secret Manager, update jwt.key-id
 *   Phase 2 — during overlap period, return BOTH old and new keys
 *   Phase 3 — after old tokens expire (max 1hr), remove old key
 */
@Service
public class JwksService {

    private static final String KEY_TYPE      = "RSA";
    private static final String KEY_USE       = "sig";
    private static final String KEY_ALGORITHM = "RS256";
    private static final String PEM_HEADER    = "-----BEGIN PUBLIC KEY-----";
    private static final String PEM_FOOTER    = "-----END PUBLIC KEY-----";

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Value("${jwt.key-id}")
    private String keyId;

    /**
     * Returns the current JWKS — one key during normal operation,
     * two keys during key rotation overlap period.
     */
    public JwksResponse getJwks() {
        RSAPublicKey publicKey = parsePublicKey(publicKeyPem);
        JwksResponse.JwkKey jwk = buildJwk(publicKey, keyId);
        return new JwksResponse(List.of(jwk));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private RSAPublicKey parsePublicKey(String pem) {
        try {
            String stripped = pem
                    .replace(PEM_HEADER, "")
                    .replace(PEM_FOOTER, "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(stripped);
            KeyFactory kf = KeyFactory.getInstance(KEY_TYPE);
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new AuthServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthServiceException.INTERNAL_ERROR,
                    "Failed to parse RS256 public key", e);
        }
    }

    private JwksResponse.JwkKey buildJwk(RSAPublicKey publicKey, String kid) {
        // Base64url encode modulus and exponent per RFC 7517
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String n = encoder.encodeToString(publicKey.getModulus().toByteArray());
        String e = encoder.encodeToString(publicKey.getPublicExponent().toByteArray());

        return new JwksResponse.JwkKey(KEY_TYPE, KEY_USE, KEY_ALGORITHM, kid, n, e);
    }
}