package com.healthcare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

    private String authServiceUrl = "http://localhost:8082";
    private int jwksRefreshIntervalMinutes = 5;
    private List<String> publicPaths = new ArrayList<>();

    public String getAuthServiceUrl() { return authServiceUrl; }
    public void setAuthServiceUrl(String authServiceUrl) { this.authServiceUrl = authServiceUrl; }

    public int getJwksRefreshIntervalMinutes() { return jwksRefreshIntervalMinutes; }
    public void setJwksRefreshIntervalMinutes(int jwksRefreshIntervalMinutes) {
        this.jwksRefreshIntervalMinutes = jwksRefreshIntervalMinutes;
    }

    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }
}
