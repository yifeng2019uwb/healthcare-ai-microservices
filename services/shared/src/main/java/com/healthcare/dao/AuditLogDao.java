package com.healthcare.dao;

import com.healthcare.entity.AuditLog;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for AuditLog entity.
 * Maps to audit_logs table — INSERT only, never update or delete.
 * HIPAA retention: minimum 6 years.
 */
@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, UUID> {

    String ERROR_UPDATE_NOT_PERMITTED = "AuditLog is INSERT only — update not permitted";
    String ERROR_DELETE_NOT_PERMITTED = "AuditLog is INSERT only — delete not permitted";

    // ==================== READ ====================

    List<AuditLog> findByAuthId(String authId);

    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, UUID resourceId);

    List<AuditLog> findByAction(ActionType action);

    List<AuditLog> findByOutcome(Outcome outcome);

    List<AuditLog> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    // ==================== INSERT only — no update, no delete ====================

    /**
     * Insert a new audit log entry.
     * Throws if entity already has an id — updates not permitted.
     */
    default AuditLog insert(AuditLog entity) {
        if (entity.getId() != null)
            throw new UnsupportedOperationException(ERROR_UPDATE_NOT_PERMITTED);
        return save(entity);
    }

    // ==================== BLOCK delete and update operations ====================

    @Override
    default void deleteById(UUID id) {
        throw new UnsupportedOperationException(ERROR_DELETE_NOT_PERMITTED);
    }

    @Override
    default void delete(AuditLog entity) {
        throw new UnsupportedOperationException(ERROR_DELETE_NOT_PERMITTED);
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException(ERROR_DELETE_NOT_PERMITTED);
    }

    @Override
    default void deleteAll(Iterable<? extends AuditLog> entities) {
        throw new UnsupportedOperationException(ERROR_DELETE_NOT_PERMITTED);
    }

    @Override
    default void deleteAllById(Iterable<? extends UUID> ids) {
        throw new UnsupportedOperationException(ERROR_DELETE_NOT_PERMITTED);
    }
}