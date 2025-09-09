package com.healthcare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Gateway Service Integration Tests
 * Uses test profile to avoid database connections
 */
@SpringBootTest
@ActiveProfiles("test")
class GatewayServiceTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        // This test verifies that the gateway service can start without database dependencies
    }
}
