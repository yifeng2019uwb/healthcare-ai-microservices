package com.healthcare.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Condition}.
 */
class ConditionEntityTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final String CODE = "44054006";
    private static final LocalDate START = LocalDate.of(2020, 1, 15);

    private ConditionId newConditionId() {
        return new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
    }

    private Condition newCondition() {
        return new Condition(newConditionId(), START);
    }

    @Test
    void constructor_setsIdAndStartDate() {
        Condition c = newCondition();
        assertThat(c.getId()).isNotNull();
        assertThat(c.getId().getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(c.getId().getEncounterId()).isEqualTo(ENCOUNTER_ID);
        assertThat(c.getId().getCode()).isEqualTo(CODE);
        assertThat(c.getStartDate()).isEqualTo(START);
        assertThat(c.getStopDate()).isNull();
        assertThat(c.getSystem()).isNull();
        assertThat(c.getDescription()).isNull();
    }

    @Test
    void getCode_delegatesToId() {
        assertThat(newCondition().getCode()).isEqualTo(CODE);
    }

    @Test
    void getCode_nullWhenIdNull() {
        Condition c = new Condition(newConditionId(), START);
        assertThat(c.getCode()).isEqualTo(CODE);
    }

    @Test
    void isOngoing_trueWhenStopDateNull() {
        assertThat(newCondition().isOngoing()).isTrue();
    }

    @Test
    void isOngoing_falseWhenStopDateSet() {
        Condition c = newCondition();
        c.setStopDate(LocalDate.of(2021, 6, 30));
        assertThat(c.isOngoing()).isFalse();
    }

    @Test
    void setters_allFields() {
        Condition c = newCondition();
        LocalDate stop = LocalDate.of(2022, 3, 10);

        c.setStopDate(stop);
        c.setSystem("SNOMED-CT");
        c.setDescription("Diabetes mellitus type 2");

        assertThat(c.getStopDate()).isEqualTo(stop);
        assertThat(c.getSystem()).isEqualTo("SNOMED-CT");
        assertThat(c.getDescription()).isEqualTo("Diabetes mellitus type 2");
    }

    @Test
    void getPatient_nullByDefault() {
        assertThat(newCondition().getPatient()).isNull();
    }

    @Test
    void getEncounter_nullByDefault() {
        assertThat(newCondition().getEncounter()).isNull();
    }

    @Test
    void setStartDate_updatesValue() {
        Condition c = newCondition();
        LocalDate newStart = LocalDate.of(2021, 6, 1);
        c.setStartDate(newStart);
        assertThat(c.getStartDate()).isEqualTo(newStart);
    }

    @Test
    void extendsBaseEntity() {
        assertThat(newCondition()).isInstanceOf(BaseEntity.class);
    }

    @Test
    void conditionId_defaultConstructor() {
        ConditionId id = new ConditionId();
        assertThat(id.getPatientId()).isNull();
        assertThat(id.getEncounterId()).isNull();
        assertThat(id.getCode()).isNull();
    }

    @Test
    void conditionId_equalsSameInstance() {
        ConditionId id = newConditionId();
        assertThat(id).isEqualTo(id);
    }

    @Test
    void conditionId_equalsSameValues() {
        ConditionId id1 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
        ConditionId id2 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void conditionId_notEqualsWhenDifferentPatientId() {
        ConditionId id1 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
        ConditionId id2 = new ConditionId(UUID.randomUUID(), ENCOUNTER_ID, CODE);
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void conditionId_notEqualsWhenDifferentEncounterId() {
        ConditionId id1 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
        ConditionId id2 = new ConditionId(PATIENT_ID, UUID.randomUUID(), CODE);
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void conditionId_notEqualsWhenDifferentCode() {
        ConditionId id1 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE);
        ConditionId id2 = new ConditionId(PATIENT_ID, ENCOUNTER_ID, "999999");
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void conditionId_notEqualsWhenDifferentType() {
        assertThat(newConditionId()).isNotEqualTo("not a ConditionId");
    }
}