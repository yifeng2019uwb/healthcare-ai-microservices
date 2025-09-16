package com.healthcare.dao;

import com.healthcare.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Data Access Object for AuditLog entity
 * Handles database operations for audit logs
 */
@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, UUID> {

    /**
     * Create a new audit log entry
     * Saves the audit log entity to the database
     * Note: Audit logs are typically not updated, only created
     *
     * @param auditLog The audit log entity to create
     * @return The created audit log entity
     */
    default AuditLog create(AuditLog auditLog) {
        return save(auditLog);
    }
}
