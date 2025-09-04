package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.enums.Gender;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MedicalRecord entity
 */
class MedicalRecordEntityTest {

    @Test
    void testMedicalRecordEntity() {
        // Test data variables
        String testPatientExternalAuthId = "ext-auth-patient3";
        String testPatientFirstName = "Patient";
        String testPatientLastName = "Three";
        String testPatientEmail = "patient3@example.com";
        String testPhone = "+1234567890";
        LocalDate testPatientDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testPatientGender = Gender.FEMALE;
        UserRole testPatientRole = UserRole.PATIENT;
        String testPatientNumber = "PAT-11111111";

        String testProviderExternalAuthId = "ext-auth-provider3";
        String testProviderFirstName = "Dr. Provider";
        String testProviderLastName = "Three";
        String testProviderEmail = "provider3@example.com";
        LocalDate testProviderDateOfBirth = LocalDate.of(1980, 1, 1);
        Gender testProviderGender = Gender.MALE;
        UserRole testProviderRole = UserRole.PROVIDER;
        String testNpiNumber = "1111111111";

        OffsetDateTime testAppointmentTime = OffsetDateTime.now().plusDays(1);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testInitialContent = "Initial Consultation - Hypertension diagnosis";
        String testUpdatedContent = "Updated diagnosis: Hypertension stage 1";
        String testCustomData = "{\"priority\": \"high\"}";
        boolean testIsPatientVisible = true;

        // Create user and patient
        User patientUser = new User(testPatientExternalAuthId, testPatientFirstName, testPatientLastName, testPatientEmail,
                                  testPhone, testPatientDateOfBirth, testPatientGender, testPatientRole);
        Patient patient = new Patient(patientUser, testPatientNumber);

        // Create user and provider
        User providerUser = new User(testProviderExternalAuthId, testProviderFirstName, testProviderLastName, testProviderEmail,
                                   testPhone, testProviderDateOfBirth, testProviderGender, testProviderRole);
        Provider provider = new Provider(providerUser, testNpiNumber);

        // Create appointment first
        Appointment appointment = new Appointment(patient, provider, testAppointmentTime, testAppointmentType);

        // Create medical record
        MedicalRecord record = new MedicalRecord(appointment, testRecordType, testInitialContent);
        record.setIsPatientVisible(testIsPatientVisible);

        // Test basic properties
        assertThat(record.getAppointment()).isEqualTo(appointment);
        assertThat(record.getRecordType()).isEqualTo(testRecordType);
        assertThat(record.getContent()).isEqualTo(testInitialContent);
        assertThat(record.getIsPatientVisible()).isTrue();

        // Test additional setters
        record.setContent(testUpdatedContent);
        record.setReleaseDate(OffsetDateTime.now().plusDays(1));
        record.setCustomData(testCustomData);

        assertThat(record.getContent()).isEqualTo(testUpdatedContent);
        assertThat(record.getReleaseDate()).isNotNull();
        assertThat(record.getCustomData()).isEqualTo(testCustomData);
    }

    @Test
    void testMedicalRecordValidationMethods() {
        // Test data variables
        String testPatientExternalAuthId = "ext-auth-patient4";
        String testPatientFirstName = "Patient";
        String testPatientLastName = "Four";
        String testPatientEmail = "patient4@example.com";
        String testPhone = "+1234567890";
        LocalDate testPatientDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testPatientGender = Gender.FEMALE;
        UserRole testPatientRole = UserRole.PATIENT;
        String testPatientNumber = "PAT-44444444";

        String testProviderExternalAuthId = "ext-auth-provider4";
        String testProviderFirstName = "Dr. Provider";
        String testProviderLastName = "Four";
        String testProviderEmail = "provider4@example.com";
        LocalDate testProviderDateOfBirth = LocalDate.of(1980, 1, 1);
        Gender testProviderGender = Gender.MALE;
        UserRole testProviderRole = UserRole.PROVIDER;
        String testNpiNumber = "4444444444";

        OffsetDateTime testAppointmentTime = OffsetDateTime.now().plusDays(1);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;
        MedicalRecordType testRecordType = MedicalRecordType.TREATMENT;
        String testContent = "Treatment plan for hypertension";

        User patientUser = new User(testPatientExternalAuthId, testPatientFirstName, testPatientLastName, testPatientEmail,
                                  testPhone, testPatientDateOfBirth, testPatientGender, testPatientRole);
        Patient patient = new Patient(patientUser, testPatientNumber);

        User providerUser = new User(testProviderExternalAuthId, testProviderFirstName, testProviderLastName, testProviderEmail,
                                   testPhone, testProviderDateOfBirth, testProviderGender, testProviderRole);
        Provider provider = new Provider(providerUser, testNpiNumber);

        Appointment appointment = new Appointment(patient, provider, testAppointmentTime, testAppointmentType);

        MedicalRecord record = new MedicalRecord(appointment, testRecordType, testContent);

        // Test validation methods
        assertThat(record.hasAppointment()).isTrue();
        assertThat(record.hasValidRecordType()).isTrue();
        assertThat(record.hasValidContent()).isTrue();
        assertThat(record.isComplete()).isTrue();

        // Test patient visibility
        assertThat(record.isVisibleToPatient()).isFalse();
        record.setIsPatientVisible(true);
        assertThat(record.isVisibleToPatient()).isTrue();

        // Test update capability (before release)
        assertThat(record.canBeUpdated()).isTrue();

        // Test release functionality
        assertThat(record.hasBeenReleased()).isFalse();
        assertThat(record.canBeReleased()).isTrue();

        record.setReleaseDate(OffsetDateTime.now().minusDays(1));
        assertThat(record.hasBeenReleased()).isTrue();
        assertThat(record.canBeReleased()).isFalse();

        // Test update capability (after release)
        assertThat(record.canBeUpdated()).isFalse(); // Can't update after release
    }

    @Test
    void testMedicalRecordTypes() {
        // Test data variables
        String testPatientExternalAuthId = "ext-auth-patient5";
        String testPatientFirstName = "Patient";
        String testPatientLastName = "Five";
        String testPatientEmail = "patient5@example.com";
        String testPhone = "+1234567890";
        LocalDate testPatientDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testPatientGender = Gender.FEMALE;
        UserRole testPatientRole = UserRole.PATIENT;
        String testPatientNumber = "PAT-55555555";

        String testProviderExternalAuthId = "ext-auth-provider5";
        String testProviderFirstName = "Dr. Provider";
        String testProviderLastName = "Five";
        String testProviderEmail = "provider5@example.com";
        LocalDate testProviderDateOfBirth = LocalDate.of(1980, 1, 1);
        Gender testProviderGender = Gender.MALE;
        UserRole testProviderRole = UserRole.PROVIDER;
        String testNpiNumber = "5555555555";

        OffsetDateTime testAppointmentTime = OffsetDateTime.now().plusDays(1);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        String testDiagnosisContent = "Hypertension diagnosis";
        String testTreatmentContent = "Prescribed medication";
        String testSummaryContent = "Visit summary";

        User patientUser = new User(testPatientExternalAuthId, testPatientFirstName, testPatientLastName, testPatientEmail,
                                  testPhone, testPatientDateOfBirth, testPatientGender, testPatientRole);
        Patient patient = new Patient(patientUser, testPatientNumber);

        User providerUser = new User(testProviderExternalAuthId, testProviderFirstName, testProviderLastName, testProviderEmail,
                                   testPhone, testProviderDateOfBirth, testProviderGender, testProviderRole);
        Provider provider = new Provider(providerUser, testNpiNumber);

        Appointment appointment = new Appointment(patient, provider, testAppointmentTime, testAppointmentType);

        // Test different record types
        MedicalRecord diagnosis = new MedicalRecord(appointment, MedicalRecordType.DIAGNOSIS, testDiagnosisContent);
        MedicalRecord treatment = new MedicalRecord(appointment, MedicalRecordType.TREATMENT, testTreatmentContent);
        MedicalRecord summary = new MedicalRecord(appointment, MedicalRecordType.SUMMARY, testSummaryContent);

        assertThat(diagnosis.getRecordType()).isEqualTo(MedicalRecordType.DIAGNOSIS);
        assertThat(treatment.getRecordType()).isEqualTo(MedicalRecordType.TREATMENT);
        assertThat(summary.getRecordType()).isEqualTo(MedicalRecordType.SUMMARY);
    }
}
