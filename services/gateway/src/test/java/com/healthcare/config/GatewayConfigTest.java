package com.healthcare.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayConfigTest {

    private final GatewayConfig config = new GatewayConfig(
            "http://auth-service",
            5,
            List.of("/api/auth/login", "/api/auth/refresh"),
            Map.of(
                    "/api/admin/",    "ADMIN",
                    "/api/patients/", "PATIENT",
                    "/api/provider/", "PROVIDER"
            )
    );

    @Test
    void getRequiredRole_pathMatchesPrefix_returnsRole() {
        assertThat(config.getRequiredRole("/api/admin/import/patients")).isEqualTo("ADMIN");
    }

    @Test
    void getRequiredRole_subpathMatchesPrefix_returnsRole() {
        assertThat(config.getRequiredRole("/api/patients/me")).isEqualTo("PATIENT");
        assertThat(config.getRequiredRole("/api/provider/patients/some-uuid/conditions"))
                .isEqualTo("PROVIDER");
    }

    @Test
    void getRequiredRole_pathMatchesNothing_returnsNull() {
        assertThat(config.getRequiredRole("/api/auth/login")).isNull();
        assertThat(config.getRequiredRole("/actuator/health")).isNull();
    }
}
