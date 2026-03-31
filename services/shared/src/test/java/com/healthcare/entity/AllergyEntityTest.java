package com.healthcare.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Allergy}.
 */
class AllergyEntityTest {

    private static final UUID PATIENT_ID   = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final String CODE       = "372687004";
    private static final LocalDate START   = LocalDate.of(2019, 5, 10);

    private AllergyId newAllergyId() {
        return new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
    }

    private Allergy newAllergy() {
        return new Allergy(newAllergyId(), START);
    }

    @Test
    void constructor_setsIdAndStartDate() {
        Allergy a = newAllergy();
        assertThat(a.getId()).isNotNull();
        assertThat(a.getId().getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(a.getId().getEncounterId()).isEqualTo(ENCOUNTER_ID);
        assertThat(a.getId().getCode()).isEqualTo(CODE);
        assertThat(a.getStartDate()).isEqualTo(START);
        assertThat(a.getStopDate()).isNull();
        assertThat(a.getAllergyType()).isNull();
        assertThat(a.getCategory()).isNull();
        assertThat(a.getReaction1()).isNull();
        assertThat(a.getReaction2()).isNull();
    }

    @Test
    void getCode_delegatesToId() {
        assertThat(newAllergy().getCode()).isEqualTo(CODE);
    }

    @Test
    void isActive_trueWhenStopDateNull() {
        assertThat(newAllergy().isActive()).isTrue();
    }

    @Test
    void isActive_falseWhenStopDateSet() {
        Allergy a = newAllergy();
        a.setStopDate(LocalDate.of(2022, 1, 1));
        assertThat(a.isActive()).isFalse();
    }

    @Test
    void setters_allFields() {
        Allergy a = newAllergy();
        LocalDate stop = LocalDate.of(2023, 4, 20);

        a.setStopDate(stop);
        a.setSystem("SNOMED-CT");
        a.setDescription("Penicillin allergy");
        a.setAllergyType("allergy");
        a.setCategory("drug");
        a.setReaction1("39579001");
        a.setDescription1("Anaphylaxis");
        a.setSeverity1("SEVERE");
        a.setReaction2("25064002");
        a.setDescription2("Headache");
        a.setSeverity2("MILD");
        a.setNotes("Patient carries EpiPen");

        assertThat(a.getStopDate()).isEqualTo(stop);
        assertThat(a.getSystem()).isEqualTo("SNOMED-CT");
        assertThat(a.getDescription()).isEqualTo("Penicillin allergy");
        assertThat(a.getAllergyType()).isEqualTo("allergy");
        assertThat(a.getCategory()).isEqualTo("drug");
        assertThat(a.getReaction1()).isEqualTo("39579001");
        assertThat(a.getDescription1()).isEqualTo("Anaphylaxis");
        assertThat(a.getSeverity1()).isEqualTo("SEVERE");
        assertThat(a.getReaction2()).isEqualTo("25064002");
        assertThat(a.getDescription2()).isEqualTo("Headache");
        assertThat(a.getSeverity2()).isEqualTo("MILD");
        assertThat(a.getNotes()).isEqualTo("Patient carries EpiPen");
    }

    @Test
    void getPatient_nullByDefault() {
        assertThat(newAllergy().getPatient()).isNull();
    }

    @Test
    void getEncounter_nullByDefault() {
        assertThat(newAllergy().getEncounter()).isNull();
    }

    @Test
    void setStartDate_updatesValue() {
        Allergy a = newAllergy();
        LocalDate newStart = LocalDate.of(2021, 3, 15);
        a.setStartDate(newStart);
        assertThat(a.getStartDate()).isEqualTo(newStart);
    }

    @Test
    void extendsBaseEntity() {
        assertThat(newAllergy()).isInstanceOf(BaseEntity.class);
    }

    @Test
    void allergyId_equalsSameValues() {
        AllergyId id1 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
        AllergyId id2 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void allergyId_equalsSameInstance() {
        AllergyId id = newAllergyId();
        assertThat(id).isEqualTo(id);
    }

    @Test
    void allergyId_notEqualsWhenDifferentPatientId() {
        AllergyId id1 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
        AllergyId id2 = new AllergyId(UUID.randomUUID(), ENCOUNTER_ID, CODE);
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void allergyId_notEqualsWhenDifferentEncounterId() {
        AllergyId id1 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
        AllergyId id2 = new AllergyId(PATIENT_ID, UUID.randomUUID(), CODE);
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void allergyId_notEqualsWhenDifferentCode() {
        AllergyId id1 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE);
        AllergyId id2 = new AllergyId(PATIENT_ID, ENCOUNTER_ID, "999999");
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void allergyId_notEqualsWhenDifferentType() {
        AllergyId id = newAllergyId();
        assertThat(id).isNotEqualTo("not an AllergyId");
    }

    @Test
    void allergyId_defaultConstructor() {
        AllergyId id = new AllergyId();
        assertThat(id.getPatientId()).isNull();
        assertThat(id.getEncounterId()).isNull();
        assertThat(id.getCode()).isNull();
    }
}