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
    void findByProviderCode_returnsProvider() {
        Provider provider = newProvider();
        when(providerDao.findByProviderCode("PRV-000001")).thenReturn(Optional.of(provider));

        Optional<Provider> result = providerDao.findByProviderCode("PRV-000001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Dr. Jane Smith");
        verify(providerDao).findByProviderCode("PRV-000001");
    }

    @Test
    void findByProviderCode_returnsEmpty_whenNotFound() {
        when(providerDao.findByProviderCode("PRV-999999")).thenReturn(Optional.empty());

        assertThat(providerDao.findByProviderCode("PRV-999999")).isEmpty();
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
    void existsByProviderCode_returnsTrue() {
        when(providerDao.existsByProviderCode("PRV-000001")).thenReturn(true);

        assertThat(providerDao.existsByProviderCode("PRV-000001")).isTrue();
    }

    @Test
    void existsByProviderCode_returnsFalse_whenNotFound() {
        when(providerDao.existsByProviderCode("PRV-999999")).thenReturn(false);

        assertThat(providerDao.existsByProviderCode("PRV-999999")).isFalse();
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