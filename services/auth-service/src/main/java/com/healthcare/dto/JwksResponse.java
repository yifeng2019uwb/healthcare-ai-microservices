package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for the JWKS endpoint (/.well-known/jwks.json).
 *
 * Follows RFC 7517 JSON Web Key Set format.
 * Gateway fetches this at startup and caches with TTL.
 * On kid miss (key rotation), gateway re-fetches automatically.
 *
 * Example response:
 * {
 *   "keys": [
 *     {
 *       "kty": "RSA",
 *       "use": "sig",
 *       "alg": "RS256",
 *       "kid": "key-2026-03",
 *       "n":   "...",
 *       "e":   "AQAB"
 *     }
 *   ]
 * }
 */
public record JwksResponse(
        List<JwkKey> keys
) {

    /**
     * Individual JSON Web Key (JWK) entry — RFC 7517.
     */
    public record JwkKey(

            @JsonProperty("kty")
            String keyType,         // "RSA"

            @JsonProperty("use")
            String use,             // "sig"

            @JsonProperty("alg")
            String algorithm,       // "RS256"

            @JsonProperty("kid")
            String keyId,           // e.g. "key-2026-03"

            @JsonProperty("n")
            String modulus,         // RSA public key modulus (Base64url encoded)

            @JsonProperty("e")
            String exponent         // RSA public key exponent (Base64url encoded)
    ) {}
}