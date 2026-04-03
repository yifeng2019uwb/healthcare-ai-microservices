package com.healthcare.dto;

import com.healthcare.entity.Patient;
import com.healthcare.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class PatientSummaryResponseTest {

    private static final OffsetDateTime LAST_ENCOUNTER =
            OffsetDateTime.of(2023, 6, 15, 9, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void from_mapsAllFields() {
        Patient patient = new Patient("MRN-000001", "John", "Doe");
        patient.setBirthdate(LocalDate.of(1990, 1, 15));
        patient.setGender(Gender.M);
        patient.setPhone("+15551234567");

        PatientSummaryResponse response = PatientSummaryResponse.from(patient, LAST_ENCOUNTER);

        assertThat(response.mrn()).isEqualTo("MRN-000001");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.birthdate()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(response.gender()).isEqualTo("M");
        assertThat(response.phone()).isEqualTo("+15551234567");
        assertThat(response.lastEncounterDate()).isEqualTo(LAST_ENCOUNTER);
    }

    @Test
    void from_handlesNullGender() {
        Patient patient = new Patient("MRN-000002", "Jane", "Doe");

        PatientSummaryResponse response = PatientSummaryResponse.from(patient, null);

        assertThat(response.gender()).isNull();
        assertThat(response.lastEncounterDate()).isNull();
    }

    @Test
    void from_handlesNullOptionalFields() {
        Patient patient = new Patient("MRN-000003", "Alex", "Smith");

        PatientSummaryResponse response = PatientSummaryResponse.from(patient, null);

        assertThat(response.birthdate()).isNull();
        assertThat(response.phone()).isNull();
        assertThat(response.lastEncounterDate()).isNull();
    }
}
