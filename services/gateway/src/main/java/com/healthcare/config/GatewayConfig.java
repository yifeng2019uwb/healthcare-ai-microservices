package com.healthcare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public record GatewayConfig(
        String authServiceUrl,
        int jwksRefreshIntervalMinutes,
        List<String> publicPaths,
        Map<String, String> rolePaths
) {
    public String getRequiredRole(String path) {
        return rolePaths.entrySet().stream()
                .filter(e -> path.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
