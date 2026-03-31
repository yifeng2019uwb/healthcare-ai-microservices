package com.healthcare.dao;

import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
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
 * Unit tests for {@link ConditionDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class ConditionDaoTest {

    @Mock
    private ConditionDao conditionDao;

    private static final UUID PATIENT_ID   = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final String CODE       = "44054006";

    private Condition newCondition() {
        return new Condition(
                new ConditionId(PATIENT_ID, ENCOUNTER_ID, CODE),
                LocalDate.of(2020, 1, 15));
    }

    @Test
    void findByIdPatientId_returnsList() {
        List<Condition> conditions = List.of(newCondition(), newCondition());
        when(conditionDao.findByIdPatientId(PATIENT_ID)).thenReturn(conditions);

        List<Condition> result = conditionDao.findByIdPatientId(PATIENT_ID);

        assertThat(result).hasSize(2);
        verify(conditionDao).findByIdPatientId(PATIENT_ID);
    }

    @Test
    void findByIdPatientId_returnsEmptyList_whenNone() {
        when(conditionDao.findByIdPatientId(PATIENT_ID)).thenReturn(List.of());

        assertThat(conditionDao.findByIdPatientId(PATIENT_ID)).isEmpty();
    }

    @Test
    void findByIdEncounterId_returnsList() {
        List<Condition> conditions = List.of(newCondition());
        when(conditionDao.findByIdEncounterId(ENCOUNTER_ID)).thenReturn(conditions);

        assertThat(conditionDao.findByIdEncounterId(ENCOUNTER_ID)).hasSize(1);
        verify(conditionDao).findByIdEncounterId(ENCOUNTER_ID);
    }

    @Test
    void findByIdPatientIdAndStopDateIsNull_returnsActiveConditions() {
        List<Condition> conditions = List.of(newCondition());
        when(conditionDao.findByIdPatientIdAndStopDateIsNull(PATIENT_ID)).thenReturn(conditions);

        List<Condition> result = conditionDao.findByIdPatientIdAndStopDateIsNull(PATIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isOngoing()).isTrue();
    }

    @Test
    void findByIdCode_returnsList() {
        List<Condition> conditions = List.of(newCondition());
        when(conditionDao.findByIdCode(CODE)).thenReturn(conditions);

        assertThat(conditionDao.findByIdCode(CODE)).hasSize(1);
    }

    @Test
    void save_returnsCondition() {
        Condition condition = newCondition();
        when(conditionDao.save(condition)).thenReturn(condition);

        assertThat(conditionDao.save(condition)).isEqualTo(condition);
        verify(conditionDao).save(condition);
    }
}