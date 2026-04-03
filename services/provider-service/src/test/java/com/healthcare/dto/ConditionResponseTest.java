package com.healthcare.dto;

import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionResponseTest {

    private Condition buildCondition() {
        ConditionId id = new ConditionId(UUID.randomUUID(), UUID.randomUUID(), "44054006");
        Condition condition = new Condition(id, LocalDate.of(2015, 3, 20));
        condition.setStopDate(LocalDate.of(2022, 7, 1));
        condition.setSystem("SNOMED-CT");
        condition.setDescription("Diabetes mellitus type 2");
        return condition;
    }

    @Test
    void from_mapsAllFields() {
        Condition condition = buildCondition();

        ConditionResponse response = ConditionResponse.from(condition);

        assertThat(response.code()).isEqualTo("44054006");
        assertThat(response.system()).isEqualTo("SNOMED-CT");
        assertThat(response.description()).isEqualTo("Diabetes mellitus type 2");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2015, 3, 20));
        assertThat(response.stopDate()).isEqualTo(LocalDate.of(2022, 7, 1));
    }

    @Test
    void from_status_isResolved_whenStopDatePresent() {
        Condition condition = buildCondition();

        ConditionResponse response = ConditionResponse.from(condition);

        assertThat(response.status()).isEqualTo("resolved");
    }

    @Test
    void from_status_isActive_whenNoStopDate() {
        ConditionId id = new ConditionId(UUID.randomUUID(), UUID.randomUUID(), "44054006");
        Condition condition = new Condition(id, LocalDate.of(2015, 3, 20));

        ConditionResponse response = ConditionResponse.from(condition);

        assertThat(response.status()).isEqualTo("active");
        assertThat(response.stopDate()).isNull();
    }

    @Test
    void from_handlesNullOptionalFields() {
        ConditionId id = new ConditionId(UUID.randomUUID(), UUID.randomUUID(), "44054006");
        Condition condition = new Condition(id, LocalDate.now());

        ConditionResponse response = ConditionResponse.from(condition);

        assertThat(response.system()).isNull();
        assertThat(response.description()).isNull();
        assertThat(response.stopDate()).isNull();
        assertThat(response.status()).isEqualTo("active");
    }
}
