package com.healthcare.entity;

import com.healthcare.enums.EncounterStatus;
import com.healthcare.enums.EncounterType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Encounter}.
 */
class EncounterEntityTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final OffsetDateTime START_TIME = OffsetDateTime.now().plusDays(1);

    private Encounter newEncounter() {
        return new Encounter(PROVIDER_ID, START_TIME);
    }

    @Test
    void constructor_setsProviderIdAndStartTime() {
        Encounter e = newEncounter();
        assertThat(e.getProviderId()).isEqualTo(PROVIDER_ID);
        assertThat(e.getStartTime()).isEqualTo(START_TIME);
        assertThat(e.getPatientId()).isNull();
        assertThat(e.getOrganizationId()).isNull();
        assertThat(e.getStatus()).isNull();
        assertThat(e.getEncounterType()).isNull();
        assertThat(e.getStopTime()).isNull();
        assertThat(e.getPayerId()).isNull();
    }

    @Test
    void isCompleted_falseWhenStopTimeNull() {
        assertThat(newEncounter().isCompleted()).isFalse();
    }

    @Test
    void isCompleted_trueWhenStopTimeSet() {
        Encounter e = newEncounter();
        e.setStopTime(START_TIME.plusHours(1));
        assertThat(e.isCompleted()).isTrue();
    }

    @Test
    void setStatus_andGetStatus() {
        Encounter e = newEncounter();
        e.setStatus(EncounterStatus.SCHEDULED);
        assertThat(e.getStatus()).isEqualTo(EncounterStatus.SCHEDULED);
    }

    @Test
    void setEncounterType_andGetEncounterType() {
        Encounter e = newEncounter();
        e.setEncounterType(EncounterType.FOLLOW_UP);
        assertThat(e.getEncounterType()).isEqualTo(EncounterType.FOLLOW_UP);
    }

    @Test
    void setters_allFields() {
        Encounter e = newEncounter();
        UUID patientId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        e.setPatientId(patientId);
        e.setOrganizationId(orgId);
        e.setPayerId("payer-123");
        e.setEncounterClass("ambulatory");
        e.setCode("185345009");
        e.setDescription("Encounter for symptom");
        e.setBaseCost(new BigDecimal("100.00"));
        e.setTotalCost(new BigDecimal("150.00"));
        e.setPayerCoverage(new BigDecimal("50.00"));
        e.setReasonCode("444814009");
        e.setReasonDesc("Viral sinusitis");

        assertThat(e.getPatientId()).isEqualTo(patientId);
        assertThat(e.getOrganizationId()).isEqualTo(orgId);
        assertThat(e.getPayerId()).isEqualTo("payer-123");
        assertThat(e.getEncounterClass()).isEqualTo("ambulatory");
        assertThat(e.getCode()).isEqualTo("185345009");
        assertThat(e.getDescription()).isEqualTo("Encounter for symptom");
        assertThat(e.getBaseCost()).isEqualByComparingTo("100.00");
        assertThat(e.getTotalCost()).isEqualByComparingTo("150.00");
        assertThat(e.getPayerCoverage()).isEqualByComparingTo("50.00");
        assertThat(e.getReasonCode()).isEqualTo("444814009");
        assertThat(e.getReasonDesc()).isEqualTo("Viral sinusitis");
    }

    @Test
    void getPatient_nullByDefault() {
        assertThat(newEncounter().getPatient()).isNull();
    }

    @Test
    void getProvider_nullByDefault() {
        assertThat(newEncounter().getProvider()).isNull();
    }

    @Test
    void getOrganization_nullByDefault() {
        assertThat(newEncounter().getOrganization()).isNull();
    }

    @Test
    void setProviderId_updatesValue() {
        Encounter e = newEncounter();
        UUID newProviderId = UUID.randomUUID();
        e.setProviderId(newProviderId);
        assertThat(e.getProviderId()).isEqualTo(newProviderId);
    }

    @Test
    void setStartTime_updatesValue() {
        Encounter e = newEncounter();
        OffsetDateTime newStart = OffsetDateTime.now().plusDays(2);
        e.setStartTime(newStart);
        assertThat(e.getStartTime()).isEqualTo(newStart);
    }

    @Test
    void extendsProfileBaseEntity() {
        assertThat(newEncounter()).isInstanceOf(ProfileBaseEntity.class);
        assertThat(newEncounter()).isInstanceOf(BaseEntity.class);
    }
}