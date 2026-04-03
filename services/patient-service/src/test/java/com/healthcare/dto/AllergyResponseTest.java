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
        allergy.setStopDate(LocalDate.of(2020, 1, 1));
        allergy.setReaction1("Wheal");
        allergy.setDescription1("Skin reaction");
        allergy.setSeverity1("MILD");
        allergy.setReaction2("Itching");
        allergy.setDescription2("Secondary reaction");
        allergy.setSeverity2("MODERATE");
        allergy.setNotes("Monitor closely");
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
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2018, 5, 10));
        assertThat(response.stopDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(response.reaction1()).isEqualTo("Wheal");
        assertThat(response.description1()).isEqualTo("Skin reaction");
        assertThat(response.severity1()).isEqualTo("MILD");
        assertThat(response.reaction2()).isEqualTo("Itching");
        assertThat(response.description2()).isEqualTo("Secondary reaction");
        assertThat(response.severity2()).isEqualTo("MODERATE");
        assertThat(response.notes()).isEqualTo("Monitor closely");
    }

    @Test
    void from_active_isFalse_whenStopDatePresent() {
        Allergy allergy = buildAllergy();

        AllergyResponse response = AllergyResponse.from(allergy);

        assertThat(response.active()).isFalse();
    }

    @Test
    void from_active_isTrue_whenNoStopDate() {
        AllergyId id = new AllergyId(UUID.randomUUID(), UUID.randomUUID(), "111088007");
        Allergy allergy = new Allergy(id, LocalDate.of(2018, 5, 10));

        AllergyResponse response = AllergyResponse.from(allergy);

        assertThat(response.active()).isTrue();
        assertThat(response.stopDate()).isNull();
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
        assertThat(response.reaction1()).isNull();
        assertThat(response.description1()).isNull();
        assertThat(response.severity1()).isNull();
        assertThat(response.reaction2()).isNull();
        assertThat(response.description2()).isNull();
        assertThat(response.severity2()).isNull();
        assertThat(response.notes()).isNull();
    }
}
