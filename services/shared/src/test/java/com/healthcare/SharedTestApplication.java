package com.healthcare;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal Spring Boot configuration for shared module tests only.
 * NOT a production application — used to bootstrap JPA context for DAO tests.
 */
@SpringBootApplication
@EntityScan("com.healthcare.entity")
@EnableJpaRepositories("com.healthcare.dao")
public class SharedTestApplication {
}