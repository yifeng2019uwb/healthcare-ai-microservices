package com.healthcare.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.config.GatewayConfig;
import com.healthcare.exception.GatewayException;
import com.healthcare.jwks.JwksCache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * GlobalFilter that runs on every request.
 * - Public paths: pass through without JWT check.
 * - Protected paths: validate RS256 JWT, check Redis blacklist, inject user headers.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final GatewayConfig config;
    private final JwksCache jwksCache;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(GatewayConfig config,
                         JwksCache jwksCache,
                         ReactiveStringRedisTemplate redisTemplate,
                         ObjectMapper objectMapper) {
        this.config = config;
        this.jwksCache = jwksCache;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (config.getPublicPaths().contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        return extractKid(token)
                .flatMap(jwksCache::getKey)
                .flatMap(publicKey -> parseAndValidate(token, publicKey))
                .flatMap(claims -> checkBlacklist(claims.get("jti", String.class)).thenReturn(claims))
                .flatMap(claims -> {
                    var mutatedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.remove(HttpHeaders.AUTHORIZATION);
                                headers.set("X-User-Id", claims.getSubject());
                                headers.set("X-User-Role", claims.get("role", String.class));
                                headers.set("X-Username", claims.get("username", String.class));
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private Mono<String> extractKid(String token) {
        try {
            String headerPart = token.split("\\.")[0];
            String headerJson = new String(Base64.getUrlDecoder().decode(headerPart));
            String kid = objectMapper.readTree(headerJson).path("kid").asText(null);
            if (kid == null || kid.isBlank()) {
                return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            }
            return Mono.just(kid);
        } catch (Exception e) {
            return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }
    }

    private Mono<Claims> parseAndValidate(String token, RSAPublicKey publicKey) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Mono.just(claims);
        } catch (JwtException e) {
            return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }
    }

    private Mono<Void> checkBlacklist(String jti) {
        if (jti == null) return Mono.empty();
        return redisTemplate.hasKey("blacklist:" + jti)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return Mono.error(new GatewayException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
                    }
                    return Mono.empty();
                })
                .onErrorResume(GatewayException.class, Mono::error)
                .onErrorResume(e -> {
                    log.error("Redis unavailable during blacklist check for jti={}: {}", jti, e.getMessage());
                    return Mono.error(new GatewayException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable"));
                })
                .then();
    }
}
