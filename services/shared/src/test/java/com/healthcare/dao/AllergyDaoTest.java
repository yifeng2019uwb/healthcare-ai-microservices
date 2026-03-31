package com.healthcare.dao;

import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AllergyDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class AllergyDaoTest {

    @Mock
    private AllergyDao allergyDao;

    private static final UUID PATIENT_ID   = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final String CODE       = "372687004";

    private Allergy newAllergy() {
        return new Allergy(
                new AllergyId(PATIENT_ID, ENCOUNTER_ID, CODE),
                LocalDate.of(2019, 5, 10));
    }

    @Test
    void findByIdPatientId_returnsList() {
        List<Allergy> allergies = List.of(newAllergy(), newAllergy());
        when(allergyDao.findByIdPatientId(PATIENT_ID)).thenReturn(allergies);

        List<Allergy> result = allergyDao.findByIdPatientId(PATIENT_ID);

        assertThat(result).hasSize(2);
        verify(allergyDao).findByIdPatientId(PATIENT_ID);
    }

    @Test
    void findByIdPatientId_returnsEmptyList_whenNone() {
        when(allergyDao.findByIdPatientId(PATIENT_ID)).thenReturn(List.of());

        assertThat(allergyDao.findByIdPatientId(PATIENT_ID)).isEmpty();
    }

    @Test
    void findByIdEncounterId_returnsList() {
        List<Allergy> allergies = List.of(newAllergy());
        when(allergyDao.findByIdEncounterId(ENCOUNTER_ID)).thenReturn(allergies);

        assertThat(allergyDao.findByIdEncounterId(ENCOUNTER_ID)).hasSize(1);
        verify(allergyDao).findByIdEncounterId(ENCOUNTER_ID);
    }

    @Test
    void findByIdPatientIdAndStopDateIsNull_returnsActiveAllergies() {
        List<Allergy> allergies = List.of(newAllergy());
        when(allergyDao.findByIdPatientIdAndStopDateIsNull(PATIENT_ID)).thenReturn(allergies);

        List<Allergy> result = allergyDao.findByIdPatientIdAndStopDateIsNull(PATIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void findByIdPatientIdAndCategory_returnsList() {
        List<Allergy> allergies = List.of(newAllergy());
        when(allergyDao.findByIdPatientIdAndCategory(PATIENT_ID, "drug")).thenReturn(allergies);

        assertThat(allergyDao.findByIdPatientIdAndCategory(PATIENT_ID, "drug")).hasSize(1);
    }

    @Test
    void save_returnsAllergy() {
        Allergy allergy = newAllergy();
        when(allergyDao.save(allergy)).thenReturn(allergy);

        assertThat(allergyDao.save(allergy)).isEqualTo(allergy);
        verify(allergyDao).save(allergy);
    }
}