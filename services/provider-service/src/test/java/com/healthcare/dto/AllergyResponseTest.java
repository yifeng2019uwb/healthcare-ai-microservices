package com.healthcare.dto;

import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AllergyResponseTest {

    private Allergy buildAllergy() {
        AllergyId id = new AllergyId(UUID.randomUUID(), UUID.randomUUID(), "111088007");
        Allergy allergy = new Allergy(id, LocalDate.of(2018, 5, 10));
        allergy.setSystem("SNOMED-CT");
        allergy.setDescription("Latex allergy");
        allergy.setAllergyType("allergy");
        allergy.setCategory("environment");
        allergy.setSeverity1("MILD");
        allergy.setStopDate(LocalDate.of(2020, 1, 1));
        return allergy;
    }

    @Test
    void from_mapsAllFields() {
        Allergy allergy = buildAllergy();

        AllergyResponse response = AllergyResponse.from(allergy);

        assertThat(response.code()).isEqualTo("111088007");
        assertThat(response.system()).isEqualTo("SNOMED-CT");
        assertThat(response.description()).isEqualTo("Latex allergy");
        assertThat(response.allergyType()).isEqualTo("allergy");
        assertThat(response.category()).isEqualTo("environment");
        assertThat(response.severity1()).isEqualTo("MILD");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2018, 5, 10));
        assertThat(response.stopDate()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void from_handlesNullOptionalFields() {
        AllergyId id = new AllergyId(UUID.randomUUID(), UUID.randomUUID(), "111088007");
        Allergy allergy = new Allergy(id, LocalDate.now());

        AllergyResponse response = AllergyResponse.from(allergy);

        assertThat(response.system()).isNull();
        assertThat(response.description()).isNull();
        assertThat(response.allergyType()).isNull();
        assertThat(response.category()).isNull();
        assertThat(response.severity1()).isNull();
        assertThat(response.stopDate()).isNull();
    }
}
