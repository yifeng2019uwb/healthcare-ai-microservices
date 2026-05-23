package com.healthcare.dao;

import com.healthcare.entity.Encounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EncounterDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class EncounterDaoTest {

    @Mock
    private EncounterDao encounterDao;

    private static final UUID PATIENT_ID  = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();

    private Encounter newEncounter() {
        return new Encounter(PROVIDER_ID, OffsetDateTime.now().plusDays(1));
    }

    @Test
    void findByPatientId_returnsList() {
        List<Encounter> encounters = List.of(newEncounter(), newEncounter());
        when(encounterDao.findByPatientId(PATIENT_ID)).thenReturn(encounters);

        List<Encounter> result = encounterDao.findByPatientId(PATIENT_ID);

        assertThat(result).hasSize(2);
        verify(encounterDao).findByPatientId(PATIENT_ID);
    }

    @Test
    void findByPatientId_returnsEmptyList_whenNone() {
        when(encounterDao.findByPatientId(PATIENT_ID)).thenReturn(List.of());

        assertThat(encounterDao.findByPatientId(PATIENT_ID)).isEmpty();
    }

    @Test
    void findByProviderId_returnsList() {
        List<Encounter> encounters = List.of(newEncounter());
        when(encounterDao.findByProviderId(PROVIDER_ID)).thenReturn(encounters);

        assertThat(encounterDao.findByProviderId(PROVIDER_ID)).hasSize(1);
        verify(encounterDao).findByProviderId(PROVIDER_ID);
    }

    @Test
    void findByProviderIdAndStartTimeBetween_returnsList() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end   = start.plusDays(7);
        List<Encounter> encounters = List.of(newEncounter());
        when(encounterDao.findByProviderIdAndStartTimeBetween(PROVIDER_ID, start, end))
                .thenReturn(encounters);

        assertThat(encounterDao.findByProviderIdAndStartTimeBetween(PROVIDER_ID, start, end))
                .hasSize(1);
    }

    @Test
    void save_returnsEncounter() {
        Encounter encounter = newEncounter();
        when(encounterDao.save(encounter)).thenReturn(encounter);

        assertThat(encounterDao.save(encounter)).isEqualTo(encounter);
        verify(encounterDao).save(encounter);
    }
}