package com.healthcare.controller;

import com.healthcare.dto.JwksResponse;
import com.healthcare.service.JwksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the JWKS endpoint for gateway token validation.
 *
 * Follows RFC 7517 JSON Web Key Set standard — same pattern used by
 * Auth0, Okta, and Google. Gateway fetches this once at startup,
 * caches with TTL, and re-fetches on kid miss (key rotation).
 *
 * This endpoint name describes what it does (validate tokens) not how —
 * the RS256 public key implementation detail is hidden behind this interface.
 * If the signing mechanism changes in future, gateway needs no code changes.
 */
@RestController
public class TokenController {

    private final JwksService jwksService;

    public TokenController(JwksService jwksService) {
        this.jwksService = jwksService;
    }

    /**
     * GET /.well-known/jwks.json
     *
     * Returns the RS256 public key set in RFC 7517 JWKS format.
     * Called by gateway at startup and on kid miss during key rotation.
     *
     * @return JWKS containing current public key(s)
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<JwksResponse> getJwks() {
        return ResponseEntity.ok(jwksService.getJwks());
    }
}