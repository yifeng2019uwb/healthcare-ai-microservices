package com.healthcare.dao;

import com.healthcare.entity.Provider;
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
 * Unit tests for {@link ProviderDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class ProviderDaoTest {

    @Mock
    private ProviderDao providerDao;

    private static final UUID ORG_ID = UUID.randomUUID();

    private Provider newProvider() {
        return new Provider(ORG_ID, "Dr. Jane Smith");
    }

    @Test
    void findByAuthId_returnsProvider() {
        UUID authId = UUID.randomUUID();
        Provider provider = newProvider();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(provider));

        assertThat(providerDao.findByAuthId(authId)).isPresent();
        verify(providerDao).findByAuthId(authId);
    }

    @Test
    void findByAuthId_returnsEmpty_whenNotFound() {
        UUID authId = UUID.randomUUID();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThat(providerDao.findByAuthId(authId)).isEmpty();
    }

    @Test
    void findByOrganizationId_returnsList() {
        List<Provider> providers = List.of(newProvider(), newProvider());
        when(providerDao.findByOrganizationId(ORG_ID)).thenReturn(providers);

        List<Provider> result = providerDao.findByOrganizationId(ORG_ID);

        assertThat(result).hasSize(2);
        verify(providerDao).findByOrganizationId(ORG_ID);
    }

    @Test
    void findByOrganizationId_returnsEmptyList_whenNone() {
        when(providerDao.findByOrganizationId(ORG_ID)).thenReturn(List.of());

        assertThat(providerDao.findByOrganizationId(ORG_ID)).isEmpty();
    }

    @Test
    void findBySpecialityAndIsActive_returnsList() {
        List<Provider> providers = List.of(newProvider());
        when(providerDao.findBySpecialityAndIsActive("Cardiology", true)).thenReturn(providers);

        List<Provider> result = providerDao.findBySpecialityAndIsActive("Cardiology", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void existsByAuthId_returnsTrue() {
        UUID authId = UUID.randomUUID();
        when(providerDao.existsByAuthId(authId)).thenReturn(true);

        assertThat(providerDao.existsByAuthId(authId)).isTrue();
    }

    @Test
    void save_returnsProvider() {
        Provider provider = newProvider();
        when(providerDao.save(provider)).thenReturn(provider);

        assertThat(providerDao.save(provider)).isEqualTo(provider);
        verify(providerDao).save(provider);
    }
}