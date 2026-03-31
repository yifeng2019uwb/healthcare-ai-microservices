package com.healthcare.entity;

import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Organization}.
 */
class OrganizationEntityTest {

    private Organization newOrg() {
        return new Organization("General Hospital");
    }

    @Test
    void constructor_setsName() {
        Organization org = newOrg();
        assertThat(org.getName()).isEqualTo("General Hospital");
        assertThat(org.getAddress()).isNull();
        assertThat(org.getCity()).isNull();
        assertThat(org.getState()).isNull();
        assertThat(org.getZip()).isNull();
        assertThat(org.getPhone()).isNull();
        assertThat(org.getLat()).isNull();
        assertThat(org.getLon()).isNull();
        assertThat(org.getRevenue()).isNull();
        assertThat(org.getUtilization()).isNull();
    }

    @Test
    void constructor_trimsName() {
        Organization org = new Organization("  City Clinic  ");
        assertThat(org.getName()).isEqualTo("City Clinic");
    }

    @Test
    void constructor_validation() {
        assertThatThrownBy(() -> new Organization(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Name is required");
        assertThatThrownBy(() -> new Organization(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Name is required");
        assertThatThrownBy(() -> new Organization("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Name is required");
    }

    @Test
    void setName_normalizesBlankToNull() {
        Organization org = newOrg();
        org.setName("New Name");
        assertThat(org.getName()).isEqualTo("New Name");
        org.setName("   ");
        assertThat(org.getName()).isNull();
    }

    @Test
    void setAddress_normalizesBlankToNull() {
        Organization org = newOrg();
        org.setAddress("123 Main St");
        assertThat(org.getAddress()).isEqualTo("123 Main St");
        org.setAddress("");
        assertThat(org.getAddress()).isNull();
    }

    @Test
    void setCity_normalizesBlankToNull() {
        Organization org = newOrg();
        org.setCity("Seattle");
        assertThat(org.getCity()).isEqualTo("Seattle");
        org.setCity("  ");
        assertThat(org.getCity()).isNull();
    }

    @Test
    void setState_normalizesBlankToNull() {
        Organization org = newOrg();
        org.setState("WA");
        assertThat(org.getState()).isEqualTo("WA");
        org.setState("");
        assertThat(org.getState()).isNull();
    }

    @Test
    void setters_simpleFields() {
        Organization org = newOrg();
        org.setZip("98101");
        org.setPhone("+12065550100");
        org.setLat(new BigDecimal("47.608013"));
        org.setLon(new BigDecimal("-122.335167"));
        org.setRevenue(new BigDecimal("1000000.00"));
        org.setUtilization(85);

        assertThat(org.getZip()).isEqualTo("98101");
        assertThat(org.getPhone()).isEqualTo("+12065550100");
        assertThat(org.getLat()).isEqualByComparingTo("47.608013");
        assertThat(org.getLon()).isEqualByComparingTo("-122.335167");
        assertThat(org.getRevenue()).isEqualByComparingTo("1000000.00");
        assertThat(org.getUtilization()).isEqualTo(85);
    }

    @Test
    void extendsProfileBaseEntity() {
        assertThat(newOrg()).isInstanceOf(ProfileBaseEntity.class);
        assertThat(newOrg()).isInstanceOf(BaseEntity.class);
    }
}