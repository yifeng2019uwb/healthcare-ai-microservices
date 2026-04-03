package com.healthcare.dto;

import com.healthcare.entity.Encounter;
import com.healthcare.enums.EncounterStatus;
import com.healthcare.enums.EncounterType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EncounterResponseTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID ORG_ID      = UUID.randomUUID();
    private static final OffsetDateTime START = OffsetDateTime.of(2023, 6, 15, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime STOP  = OffsetDateTime.of(2023, 6, 15, 9, 30, 0, 0, ZoneOffset.UTC);

    private Encounter buildEncounter() {
        Encounter e = new Encounter(PROVIDER_ID, START);
        e.setOrganizationId(ORG_ID);
        e.setStopTime(STOP);
        e.setEncounterClass("ambulatory");
        e.setStatus(EncounterStatus.COMPLETED);
        e.setEncounterType(EncounterType.FOLLOW_UP);
        e.setCode("185349003");
        e.setDescription("Encounter for check up");
        e.setBaseCost(new BigDecimal("129.16"));
        e.setTotalCost(new BigDecimal("129.16"));
        e.setReasonCode("44054006");
        e.setReasonDesc("Diabetes mellitus type 2");
        return e;
    }

    @Test
    void from_mapsAllFields() {
        Encounter encounter = buildEncounter();

        EncounterResponse response = EncounterResponse.from(encounter);

        assertThat(response.providerId()).isEqualTo(PROVIDER_ID);
        assertThat(response.organizationId()).isEqualTo(ORG_ID);
        assertThat(response.startTime()).isEqualTo(START);
        assertThat(response.stopTime()).isEqualTo(STOP);
        assertThat(response.encounterClass()).isEqualTo("ambulatory");
        assertThat(response.status()).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(response.encounterType()).isEqualTo(EncounterType.FOLLOW_UP);
        assertThat(response.code()).isEqualTo("185349003");
        assertThat(response.description()).isEqualTo("Encounter for check up");
        assertThat(response.baseCost()).isEqualByComparingTo(new BigDecimal("129.16"));
        assertThat(response.totalCost()).isEqualByComparingTo(new BigDecimal("129.16"));
        assertThat(response.reasonCode()).isEqualTo("44054006");
        assertThat(response.reasonDesc()).isEqualTo("Diabetes mellitus type 2");
    }

    @Test
    void from_handlesNullOptionalFields() {
        Encounter encounter = new Encounter(PROVIDER_ID, START);

        EncounterResponse response = EncounterResponse.from(encounter);

        assertThat(response.organizationId()).isNull();
        assertThat(response.stopTime()).isNull();
        assertThat(response.encounterClass()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.encounterType()).isNull();
        assertThat(response.code()).isNull();
        assertThat(response.description()).isNull();
        assertThat(response.baseCost()).isNull();
        assertThat(response.totalCost()).isNull();
        assertThat(response.reasonCode()).isNull();
        assertThat(response.reasonDesc()).isNull();
    }
}
