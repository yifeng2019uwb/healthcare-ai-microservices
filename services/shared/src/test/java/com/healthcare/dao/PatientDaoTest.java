package com.healthcare.dao;

import com.healthcare.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Manual DB connection test for PatientDao.
 * Connects directly to Cloud SQL — not for CI/CD.
 *
 * Before running, set environment variables:
 *   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
 *   SPRING_DATASOURCE_USERNAME=<username>
 *   SPRING_DATASOURCE_PASSWORD=<password>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PatientDaoTest {

    @Autowired
    private PatientDao patientDao;

    @Test
    void testConnection() {
        long count = patientDao.count();
        System.out.println("✅ DB connected — patient count: " + count);
        assertTrue(count >= 0);
    }

    @Test
    void testFindAll() {
        List<Patient> patients = patientDao.findAll();
        System.out.println("✅ Found " + patients.size() + " patients");
        assertNotNull(patients);
    }

    @Test
    void testFindByMrn() {
        // Replace with an actual MRN from your DB
        String testMrn = "MRN-000001";
        Optional<Patient> patient = patientDao.findByMrn(testMrn);
        if (patient.isPresent()) {
            System.out.println("✅ Found patient: " + patient.get().getFullName());
        } else {
            System.out.println("ℹ️ No patient found with MRN: " + testMrn);
        }
        // Just verify query runs without error
        assertNotNull(patient);
    }
}