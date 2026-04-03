package com.healthcare.dto;

import com.healthcare.entity.Patient;
import com.healthcare.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterPatientResponseTest {

    @Test
    void from_mapsAllFields() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");
        patient.setBirthdate(LocalDate.of(1990, 1, 15));
        patient.setGender(Gender.F);

        RegisterPatientResponse response = RegisterPatientResponse.from(patient);

        assertThat(response.mrn()).isEqualTo("MRN-000001");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.birthdate()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(response.gender()).isEqualTo("F");
        assertThat(response.createdAt()).isNull(); // set by DB after persist
    }

    @Test
    void from_handlesNullGender() {
        Patient patient = new Patient("MRN-000002", "Jane", "Smith");

        RegisterPatientResponse response = RegisterPatientResponse.from(patient);

        assertThat(response.gender()).isNull();
    }

    @Test
    void from_handlesNullOptionalFields() {
        Patient patient = new Patient("MRN-000003", "Alex", "Brown");

        RegisterPatientResponse response = RegisterPatientResponse.from(patient);

        assertThat(response.birthdate()).isNull();
        assertThat(response.gender()).isNull();
        assertThat(response.createdAt()).isNull();
    }
}
