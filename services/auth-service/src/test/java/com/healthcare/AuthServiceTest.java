package com.healthcare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Auth Service Integration Tests
 * Uses test profile with H2 in-memory database
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        // This test verifies that the auth service can start with H2 test database
    }
}
