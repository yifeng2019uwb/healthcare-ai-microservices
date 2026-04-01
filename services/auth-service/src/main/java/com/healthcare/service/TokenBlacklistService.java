package com.healthcare.service;

import com.healthcare.exception.AuthServiceException;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Manages the JWT blacklist in Redis (GCP Cloud Memorystore).
 *
 * Key pattern: blacklist:{jti}
 * Value:       "1"
 * TTL:         remaining token lifetime — auto-expires when JWT would have expired
 *
 * On logout:   both access token jti and refresh token jti are blacklisted
 * On refresh:  old refresh token jti is blacklisted immediately (rotation)
 * On validate: gateway checks Redis directly — auth-service does not check per-request
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BLACKLIST_VALUE  = "1";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    /**
     * Blacklists a token by its JTI with TTL matching remaining token lifetime.
     * Redis auto-expires the entry when the JWT itself would have expired.
     *
     * @param claims parsed and validated token claims
     */
    public void blacklist(Claims claims) {
        String jti = jwtService.extractJti(claims);
        long ttlSeconds = jwtService.getRemainingTtlSeconds(claims);

        if (ttlSeconds <= 0) {
            // Token already expired — no need to blacklist, gateway rejects on exp
            log.debug("Token jti={} already expired, skipping blacklist", jti);
            return;
        }

        try {
            String key = BLACKLIST_PREFIX + jti;
            redisTemplate.opsForValue().set(key, BLACKLIST_VALUE, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Blacklisted token jti={} ttl={}s", jti, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to blacklist token jti={}: {}", jti, e.getMessage(), e);
            throw new AuthServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthServiceException.INTERNAL_ERROR,
                    "Failed to invalidate token", e);
        }
    }

    /**
     * Checks whether a token JTI is blacklisted.
     * Called by auth-service during refresh — gateway checks Redis directly for validate.
     *
     * @param jti the token's unique identifier
     * @return true if blacklisted
     */
    public boolean isBlacklisted(String jti) {
        try {
            String key = BLACKLIST_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            // Fail closed — cannot confirm token is not revoked, so reject it.
            // A revoked token accepted due to Redis downtime is a security breach.
            log.error("Redis blacklist check failed for jti={}: {}", jti, e.getMessage(), e);
            throw new AuthServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    AuthServiceException.INTERNAL_ERROR,
                    "Token blacklist unavailable, cannot verify token jti=" + jti, e);
        }
    }
}