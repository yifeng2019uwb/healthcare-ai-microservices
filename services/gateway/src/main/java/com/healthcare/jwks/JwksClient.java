package com.healthcare.jwks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.config.GatewayConfig;
import com.healthcare.exception.GatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwksClient {

    private static final Logger log = LoggerFactory.getLogger(JwksClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public JwksClient(GatewayConfig config, ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(config.getAuthServiceUrl())
                .build();
        this.objectMapper = objectMapper;
    }

    public Mono<Map<String, RSAPublicKey>> fetchKeys() {
        return webClient.get()
                .uri("/.well-known/jwks.json")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJwks)
                .doOnError(e -> log.error("Failed to fetch JWKS from auth-service: {}", e.getMessage()));
    }

    private Map<String, RSAPublicKey> parseJwks(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            Map<String, RSAPublicKey> keys = new HashMap<>();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            for (JsonNode keyNode : root.get("keys")) {
                if (!"RSA".equals(keyNode.path("kty").asText())) continue;

                String kid = keyNode.path("kid").asText();
                byte[] nBytes = Base64.getUrlDecoder().decode(keyNode.path("n").asText());
                byte[] eBytes = Base64.getUrlDecoder().decode(keyNode.path("e").asText());

                RSAPublicKeySpec spec = new RSAPublicKeySpec(
                        new BigInteger(1, nBytes),
                        new BigInteger(1, eBytes)
                );
                keys.put(kid, (RSAPublicKey) keyFactory.generatePublic(spec));
            }

            log.debug("Parsed {} RSA key(s) from JWKS", keys.size());
            return keys;
        } catch (Exception e) {
            throw new GatewayException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to parse JWKS");
        }
    }
}
