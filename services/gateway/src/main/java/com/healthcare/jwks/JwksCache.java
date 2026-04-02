package com.healthcare.jwks;

import com.healthcare.config.GatewayConfig;
import com.healthcare.exception.GatewayException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory JWKS cache.
 * On startup: fetches keys from auth-service.
 * On kid miss: re-fetches once, rate-limited to max once per jwksRefreshIntervalMinutes.
 */
@Component
public class JwksCache {

    private static final Logger log = LoggerFactory.getLogger(JwksCache.class);

    private final JwksClient jwksClient;
    private final long refreshIntervalMs;

    private final AtomicReference<Map<String, RSAPublicKey>> cache =
            new AtomicReference<>(Collections.emptyMap());
    private final AtomicLong lastRefreshMs = new AtomicLong(0);

    public JwksCache(JwksClient jwksClient, GatewayConfig config) {
        this.jwksClient = jwksClient;
        this.refreshIntervalMs = config.getJwksRefreshIntervalMinutes() * 60_000L;
    }

    @PostConstruct
    public void init() {
        jwksClient.fetchKeys()
                .doOnNext(keys -> {
                    cache.set(keys);
                    lastRefreshMs.set(System.currentTimeMillis());
                    log.info("JWKS cache initialized: {} key(s)", keys.size());
                })
                .doOnError(e -> log.warn("Initial JWKS fetch failed, will retry on first request: {}", e.getMessage()))
                .subscribe();
    }

    public Mono<RSAPublicKey> getKey(String kid) {
        RSAPublicKey cached = cache.get().get(kid);
        if (cached != null) {
            return Mono.just(cached);
        }

        // kid miss — refresh if rate limit allows
        long now = System.currentTimeMillis();
        if (now - lastRefreshMs.get() < refreshIntervalMs) {
            return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        log.info("kid={} not found in cache, refreshing JWKS", kid);
        return jwksClient.fetchKeys()
                .doOnNext(keys -> {
                    cache.set(keys);
                    lastRefreshMs.set(System.currentTimeMillis());
                })
                .flatMap(keys -> {
                    RSAPublicKey key = keys.get(kid);
                    if (key == null) {
                        return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
                    }
                    return Mono.just(key);
                });
    }
}
