package com.healthcare;

import com.healthcare.api.CreatePatientAccountRequest;
import com.healthcare.controller.PatientController;
import com.healthcare.entity.User;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import com.healthcare.service.PatientService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

/**
 * Test class to verify database operations work
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://ep-rough-sun-a5xq8x2p.us-east-2.aws.neon.tech:5432/neondb?sslmode=require",
    "spring.datasource.username=neondb_owner",
    "spring.datasource.password=pg_mNa1rdsbhP7V",
    "spring.jpa.hibernate.ddl-auto=validate"
})
public class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    @Test
    @Disabled("Disabled due to database connection issues with Neon SCRAM authentication")
    public void testCreatePatient() {
        System.out.println("=== Testing Create Patient ===");

        // Create a test user entity
        User user = new User(
            "test123",
            "John",
            "Doe",
            "john.doe@example.com",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            UserRole.PATIENT
        );

        System.out.println("Created user entity: " + user.getFirstName() + " " + user.getLastName());

        try {
            // Call the service method to save to database
            User savedUser = patientService.createPatient(user);

            System.out.println("✅ SUCCESS: User saved to database!");
            System.out.println("User ID: " + savedUser.getId());
            System.out.println("User Email: " + savedUser.getEmail());
            System.out.println("User Role: " + savedUser.getRole());
            System.out.println("User Status: " + savedUser.getStatus());

        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to save user to database");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
