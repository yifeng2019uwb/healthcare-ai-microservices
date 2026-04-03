package com.healthcare.dto;

import com.healthcare.entity.Patient;
import com.healthcare.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientProfileResponseTest {

    @Test
    void from_mapsAllFields() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");
        patient.setMiddleName("A");
        patient.setBirthdate(LocalDate.of(1990, 1, 15));
        patient.setGender(Gender.M);
        patient.setPhone("+15551234567");
        patient.setAddress("123 Main St");
        patient.setCity("Seattle");
        patient.setState("WA");
        patient.setZip("98101");
        patient.setBloodType("A+");
        patient.setNotes("Test notes");

        PatientProfileResponse response = PatientProfileResponse.from(patient);

        assertThat(response.mrn()).isEqualTo("MRN-000001");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.middleName()).isEqualTo("A");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.birthdate()).isEqualTo("1990-01-15");
        assertThat(response.gender()).isEqualTo(Gender.M);
        assertThat(response.phone()).isEqualTo("+15551234567");
        assertThat(response.address()).isEqualTo("123 Main St");
        assertThat(response.city()).isEqualTo("Seattle");
        assertThat(response.state()).isEqualTo("WA");
        assertThat(response.zip()).isEqualTo("98101");
        assertThat(response.bloodType()).isEqualTo("A+");
        assertThat(response.notes()).isEqualTo("Test notes");
    }

    @Test
    void from_handlesNullBirthdate() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");

        PatientProfileResponse response = PatientProfileResponse.from(patient);

        assertThat(response.birthdate()).isNull();
    }

    @Test
    void from_mapsPrefixSuffixRaceEthnicityEmergencyContact() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");
        patient.setPrefix("Mr");
        patient.setSuffix("Jr");
        patient.setRace("white");
        patient.setEthnicity("nonhispanic");
        patient.setEmergencyContact("Jane Doe - 206-555-0101");

        PatientProfileResponse response = PatientProfileResponse.from(patient);

        assertThat(response.prefix()).isEqualTo("Mr");
        assertThat(response.suffix()).isEqualTo("Jr");
        assertThat(response.race()).isEqualTo("white");
        assertThat(response.ethnicity()).isEqualTo("nonhispanic");
        assertThat(response.emergencyContact()).isEqualTo("Jane Doe - 206-555-0101");
    }

    @Test
    void from_handlesNullOptionalFields() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");

        PatientProfileResponse response = PatientProfileResponse.from(patient);

        assertThat(response.middleName()).isNull();
        assertThat(response.phone()).isNull();
        assertThat(response.gender()).isNull();
        assertThat(response.notes()).isNull();
        assertThat(response.prefix()).isNull();
        assertThat(response.suffix()).isNull();
        assertThat(response.race()).isNull();
        assertThat(response.ethnicity()).isNull();
        assertThat(response.emergencyContact()).isNull();
        assertThat(response.createdAt()).isNull();
        assertThat(response.updatedAt()).isNull();
    }
}
