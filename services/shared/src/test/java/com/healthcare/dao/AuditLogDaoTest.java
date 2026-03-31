package com.healthcare.dao;

import com.healthcare.entity.AuditLog;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuditLogDao} — mocked, no DB required.
 * Also tests INSERT-only enforcement via default methods.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogDaoTest {

    @Mock
    private AuditLogDao auditLogDao;

    private AuditLog newLog() {
        return new AuditLog(ActionType.READ, "patients", Outcome.SUCCESS)
                .withAuthId("user-123")
                .withUserRole(UserRole.PATIENT);
    }

    @Test
    void findByAuthId_returnsList() {
        List<AuditLog> logs = List.of(newLog(), newLog());
        when(auditLogDao.findByAuthId("user-123")).thenReturn(logs);

        List<AuditLog> result = auditLogDao.findByAuthId("user-123");

        assertThat(result).hasSize(2);
        verify(auditLogDao).findByAuthId("user-123");
    }

    @Test
    void findByAuthId_returnsEmptyList_whenNone() {
        when(auditLogDao.findByAuthId("unknown")).thenReturn(List.of());

        assertThat(auditLogDao.findByAuthId("unknown")).isEmpty();
    }

    @Test
    void findByResourceTypeAndResourceId_returnsList() {
        UUID resourceId = UUID.randomUUID();
        List<AuditLog> logs = List.of(newLog());
        when(auditLogDao.findByResourceTypeAndResourceId("patients", resourceId)).thenReturn(logs);

        assertThat(auditLogDao.findByResourceTypeAndResourceId("patients", resourceId)).hasSize(1);
    }

    @Test
    void findByAction_returnsList() {
        List<AuditLog> logs = List.of(newLog());
        when(auditLogDao.findByAction(ActionType.READ)).thenReturn(logs);

        assertThat(auditLogDao.findByAction(ActionType.READ)).hasSize(1);
    }

    @Test
    void findByOutcome_returnsList() {
        List<AuditLog> logs = List.of(newLog());
        when(auditLogDao.findByOutcome(Outcome.SUCCESS)).thenReturn(logs);

        assertThat(auditLogDao.findByOutcome(Outcome.SUCCESS)).hasSize(1);
    }

    @Test
    void findByCreatedAtBetween_returnsList() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();
        List<AuditLog> logs  = List.of(newLog());
        when(auditLogDao.findByCreatedAtBetween(start, end)).thenReturn(logs);

        assertThat(auditLogDao.findByCreatedAtBetween(start, end)).hasSize(1);
    }

    @Test
    void insert_callsSave_whenIdIsNull() {
        AuditLog log = newLog();
        // id is null — should call save and return the entity
        AuditLog result = realDefaults.insert(log);
        assertThat(result).isEqualTo(log);
    }

    // ==================== INSERT-only enforcement — test default methods directly ====================
    // @Mock intercepts default methods silently, so we use an anonymous stub
    // that delegates save() to allow insert() to work, and lets defaults run naturally.

    private final AuditLogDao realDefaults = new AuditLogDao() {
        @Override
        public <S extends AuditLog> S save(S entity) { return entity; }
        // All other JpaRepository methods unused in these tests
        @Override public java.util.List<AuditLog> findAll() { return List.of(); }
        @Override public java.util.List<AuditLog> findAll(org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public org.springframework.data.domain.Page<AuditLog> findAll(org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
        @Override public java.util.List<AuditLog> findAllById(Iterable<UUID> uuids) { return List.of(); }
        @Override public <S extends AuditLog> java.util.List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public java.util.Optional<AuditLog> findById(UUID uuid) { return java.util.Optional.empty(); }
        @Override public boolean existsById(UUID uuid) { return false; }
        @Override public long count() { return 0; }
        @Override public void flush() {}
        @Override public <S extends AuditLog> S saveAndFlush(S entity) { return entity; }
        @Override public <S extends AuditLog> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<AuditLog> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
        @Override public void deleteAllInBatch() {}
        @Override public AuditLog getOne(UUID uuid) { return null; }
        @Override public AuditLog getById(UUID uuid) { return null; }
        @Override public AuditLog getReferenceById(UUID uuid) { return null; }
        @Override public <S extends AuditLog> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return java.util.Optional.empty(); }
        @Override public <S extends AuditLog> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends AuditLog> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends AuditLog> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends AuditLog> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends AuditLog> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends AuditLog, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public java.util.List<AuditLog> findByAuthId(String authId) { return List.of(); }
        @Override public java.util.List<AuditLog> findByResourceTypeAndResourceId(String resourceType, UUID resourceId) { return List.of(); }
        @Override public java.util.List<AuditLog> findByAction(ActionType action) { return List.of(); }
        @Override public java.util.List<AuditLog> findByOutcome(Outcome outcome) { return List.of(); }
        @Override public java.util.List<AuditLog> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) { return List.of(); }
    };

    @Test
    void insert_throwsWhenIdAlreadySet() throws Exception {
        AuditLog log = newLog();
        var idField = AuditLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(log, UUID.randomUUID());

        assertThatThrownBy(() -> realDefaults.insert(log))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_UPDATE_NOT_PERMITTED);
    }

    @Test
    void deleteById_throwsUnsupported() {
        assertThatThrownBy(() -> realDefaults.deleteById(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_DELETE_NOT_PERMITTED);
    }

    @Test
    void delete_throwsUnsupported() {
        assertThatThrownBy(() -> realDefaults.delete(newLog()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_DELETE_NOT_PERMITTED);
    }

    @Test
    void deleteAll_throwsUnsupported() {
        assertThatThrownBy(() -> realDefaults.deleteAll())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_DELETE_NOT_PERMITTED);
    }

    @Test
    void deleteAllEntities_throwsUnsupported() {
        assertThatThrownBy(() -> realDefaults.deleteAll(List.of(newLog())))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_DELETE_NOT_PERMITTED);
    }

    @Test
    void deleteAllById_throwsUnsupported() {
        assertThatThrownBy(() -> realDefaults.deleteAllById(List.of(UUID.randomUUID())))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(AuditLogDao.ERROR_DELETE_NOT_PERMITTED);
    }
}