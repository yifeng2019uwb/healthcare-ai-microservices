package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * API Response for health check
 * 
 * This matches the design document specification for GET /health
 * 
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("service")
    private String service;

    @JsonProperty("version")
    private String version;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("database")
    private String database;

    @JsonProperty("dependencies")
    private Map<String, String> dependencies;
}
