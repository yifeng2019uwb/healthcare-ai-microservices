package com.healthcare.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.config.GatewayConfig;
import com.healthcare.constants.SecurityConstants;
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
 * - Protected paths: validate RS256 JWT, inject user headers.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final int    JWT_FILTER_ORDER = -100;
    private static final String BEARER_PREFIX    = "Bearer ";

    private final GatewayConfig config;
    private final JwksCache jwksCache;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(GatewayConfig config,
                         JwksCache jwksCache,
                         ObjectMapper objectMapper) {
        this.config = config;
        this.jwksCache = jwksCache;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return JWT_FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (config.publicPaths().contains(path)) {
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
                .flatMap(claims -> checkRole(path, claims.get(SecurityConstants.JWT_CLAIM_ROLE, String.class)).thenReturn(claims))
                .flatMap(claims -> {
                    var mutatedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.set(SecurityConstants.HEADER_USER_ID,   claims.getSubject());
                                headers.set(SecurityConstants.HEADER_USER_ROLE, claims.get(SecurityConstants.JWT_CLAIM_ROLE, String.class));
                                headers.set(SecurityConstants.HEADER_USERNAME,  claims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class));
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private Mono<Void> checkRole(String path, String role) {
        String required = config.getRequiredRole(path);
        if (required == null) return Mono.empty();
        if (required.equals(role)) return Mono.empty();
        log.warn("RBAC denied: path={} required={} actual={}", path, required, role);
        return Mono.error(new GatewayException(HttpStatus.FORBIDDEN, "Forbidden"));
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
}
