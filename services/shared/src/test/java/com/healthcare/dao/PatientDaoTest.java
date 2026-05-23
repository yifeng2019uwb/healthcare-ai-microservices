package com.healthcare.dao;

import com.healthcare.entity.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PatientDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class PatientDaoTest {

    @Mock
    private PatientDao patientDao;

    private Patient newPatient() {
        return new Patient("John", "Doe");
    }

    @Test
    void findByAuthId_returnsPatient() {
        UUID authId = UUID.randomUUID();
        Patient patient = newPatient();
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));

        assertThat(patientDao.findByAuthId(authId)).isPresent();
        verify(patientDao).findByAuthId(authId);
    }

    @Test
    void findByAuthId_returnsEmpty_whenNotFound() {
        UUID authId = UUID.randomUUID();
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThat(patientDao.findByAuthId(authId)).isEmpty();
    }

    @Test
    void existsByAuthId_returnsTrue() {
        UUID authId = UUID.randomUUID();
        when(patientDao.existsByAuthId(authId)).thenReturn(true);

        assertThat(patientDao.existsByAuthId(authId)).isTrue();
    }

    @Test
    void existsByAuthId_returnsFalse_whenNotLinked() {
        UUID authId = UUID.randomUUID();
        when(patientDao.existsByAuthId(authId)).thenReturn(false);

        assertThat(patientDao.existsByAuthId(authId)).isFalse();
    }

    @Test
    void save_returnsPatient() {
        Patient patient = newPatient();
        when(patientDao.save(patient)).thenReturn(patient);

        assertThat(patientDao.save(patient)).isEqualTo(patient);
        verify(patientDao).save(patient);
    }
}