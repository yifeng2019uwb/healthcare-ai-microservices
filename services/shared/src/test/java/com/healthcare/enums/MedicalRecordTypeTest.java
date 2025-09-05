package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MedicalRecordType enum
 */
class MedicalRecordTypeTest {

    @Test
    void testMedicalRecordTypeValues() {
        assertThat(MedicalRecordType.values()).hasSize(7);
        assertThat(MedicalRecordType.DIAGNOSIS).isNotNull();
        assertThat(MedicalRecordType.TREATMENT).isNotNull();
        assertThat(MedicalRecordType.SUMMARY).isNotNull();
        assertThat(MedicalRecordType.LAB_RESULT).isNotNull();
        assertThat(MedicalRecordType.PRESCRIPTION).isNotNull();
        assertThat(MedicalRecordType.NOTE).isNotNull();
        assertThat(MedicalRecordType.OTHER).isNotNull();
    }

    @Test
    void testMedicalRecordTypeCodes() {
        assertThat(MedicalRecordType.DIAGNOSIS.getCode()).isEqualTo("DIAGNOSIS");
        assertThat(MedicalRecordType.TREATMENT.getCode()).isEqualTo("TREATMENT");
        assertThat(MedicalRecordType.SUMMARY.getCode()).isEqualTo("SUMMARY");
        assertThat(MedicalRecordType.LAB_RESULT.getCode()).isEqualTo("LAB_RESULT");
        assertThat(MedicalRecordType.PRESCRIPTION.getCode()).isEqualTo("PRESCRIPTION");
        assertThat(MedicalRecordType.NOTE.getCode()).isEqualTo("NOTE");
        assertThat(MedicalRecordType.OTHER.getCode()).isEqualTo("OTHER");
    }

    @Test
    void testMedicalRecordTypeDescriptions() {
        assertThat(MedicalRecordType.DIAGNOSIS.getDescription()).isEqualTo("Diagnosis information");
        assertThat(MedicalRecordType.TREATMENT.getDescription()).isEqualTo("Treatment details");
        assertThat(MedicalRecordType.SUMMARY.getDescription()).isEqualTo("Visit summary");
        assertThat(MedicalRecordType.LAB_RESULT.getDescription()).isEqualTo("Laboratory results");
        assertThat(MedicalRecordType.PRESCRIPTION.getDescription()).isEqualTo("Prescription details");
        assertThat(MedicalRecordType.NOTE.getDescription()).isEqualTo("Clinical notes");
        assertThat(MedicalRecordType.OTHER.getDescription()).isEqualTo("Other medical records");
    }

    @Test
    void testMedicalRecordTypeValueOf() {
        assertThat(MedicalRecordType.valueOf("DIAGNOSIS")).isEqualTo(MedicalRecordType.DIAGNOSIS);
        assertThat(MedicalRecordType.valueOf("TREATMENT")).isEqualTo(MedicalRecordType.TREATMENT);
        assertThat(MedicalRecordType.valueOf("SUMMARY")).isEqualTo(MedicalRecordType.SUMMARY);
        assertThat(MedicalRecordType.valueOf("LAB_RESULT")).isEqualTo(MedicalRecordType.LAB_RESULT);
        assertThat(MedicalRecordType.valueOf("PRESCRIPTION")).isEqualTo(MedicalRecordType.PRESCRIPTION);
        assertThat(MedicalRecordType.valueOf("NOTE")).isEqualTo(MedicalRecordType.NOTE);
        assertThat(MedicalRecordType.valueOf("OTHER")).isEqualTo(MedicalRecordType.OTHER);
    }
}
