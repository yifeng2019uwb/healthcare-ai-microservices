package com.healthcare.dao;

import com.healthcare.entity.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrganizationDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationDaoTest {

    @Mock
    private OrganizationDao organizationDao;

    private Organization newOrg() {
        return new Organization("General Hospital");
    }

    @Test
    void findByName_returnsOrganization() {
        Organization org = newOrg();
        when(organizationDao.findByName("General Hospital")).thenReturn(Optional.of(org));

        Optional<Organization> result = organizationDao.findByName("General Hospital");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("General Hospital");
        verify(organizationDao).findByName("General Hospital");
    }

    @Test
    void findByName_returnsEmpty_whenNotFound() {
        when(organizationDao.findByName("Unknown")).thenReturn(Optional.empty());

        assertThat(organizationDao.findByName("Unknown")).isEmpty();
    }

    @Test
    void findByCity_returnsList() {
        List<Organization> orgs = List.of(newOrg(), newOrg());
        when(organizationDao.findByCity("Seattle")).thenReturn(orgs);

        List<Organization> result = organizationDao.findByCity("Seattle");

        assertThat(result).hasSize(2);
        verify(organizationDao).findByCity("Seattle");
    }

    @Test
    void findByCity_returnsEmptyList_whenNone() {
        when(organizationDao.findByCity("Unknown City")).thenReturn(List.of());

        assertThat(organizationDao.findByCity("Unknown City")).isEmpty();
    }

    @Test
    void findByState_returnsList() {
        List<Organization> orgs = List.of(newOrg());
        when(organizationDao.findByState("WA")).thenReturn(orgs);

        assertThat(organizationDao.findByState("WA")).hasSize(1);
    }

    @Test
    void existsByName_returnsTrue() {
        when(organizationDao.existsByName("General Hospital")).thenReturn(true);

        assertThat(organizationDao.existsByName("General Hospital")).isTrue();
    }

    @Test
    void existsByName_returnsFalse_whenNotFound() {
        when(organizationDao.existsByName("Unknown")).thenReturn(false);

        assertThat(organizationDao.existsByName("Unknown")).isFalse();
    }

    @Test
    void save_returnsOrganization() {
        Organization org = newOrg();
        when(organizationDao.save(org)).thenReturn(org);

        assertThat(organizationDao.save(org)).isEqualTo(org);
        verify(organizationDao).save(org);
    }

    @Test
    void findById_returnsOrganization() {
        UUID id = UUID.randomUUID();
        Organization org = newOrg();
        when(organizationDao.findById(id)).thenReturn(Optional.of(org));

        assertThat(organizationDao.findById(id)).isPresent();
    }
}