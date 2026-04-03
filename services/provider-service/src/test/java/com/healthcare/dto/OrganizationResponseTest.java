package com.healthcare.dto;

import com.healthcare.entity.Organization;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationResponseTest {

    @Test
    void from_mapsAllFields() {
        Organization org = new Organization("General Hospital");
        org.setAddress("100 Main St");
        org.setCity("Seattle");
        org.setState("WA");
        org.setPhone("+12065550100");

        OrganizationResponse response = OrganizationResponse.from(org);

        assertThat(response.name()).isEqualTo("General Hospital");
        assertThat(response.address()).isEqualTo("100 Main St");
        assertThat(response.city()).isEqualTo("Seattle");
        assertThat(response.state()).isEqualTo("WA");
        assertThat(response.phone()).isEqualTo("+12065550100");
    }

    @Test
    void from_returnsNull_whenOrganizationIsNull() {
        assertThat(OrganizationResponse.from(null)).isNull();
    }

    @Test
    void from_handlesNullOptionalFields() {
        Organization org = new Organization("Minimal Clinic");

        OrganizationResponse response = OrganizationResponse.from(org);

        assertThat(response.name()).isEqualTo("Minimal Clinic");
        assertThat(response.address()).isNull();
        assertThat(response.city()).isNull();
        assertThat(response.state()).isNull();
        assertThat(response.phone()).isNull();
    }
}
