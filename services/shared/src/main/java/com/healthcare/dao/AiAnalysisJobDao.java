package com.healthcare.dao;

import com.healthcare.entity.AiAnalysisJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AiAnalysisJobDao extends JpaRepository<AiAnalysisJob, UUID> {

    /**
     * Find jobs ready for processing:
     * - PENDING status
     * - debounce window elapsed (markedAt older than cutoff)
     * - no active lock (lock expired or not set)
     * - retry delay elapsed (nextRetryAt in the past or not set)
     */
    @Query("SELECT j FROM AiAnalysisJob j WHERE j.status = 'PENDING' " +
           "AND j.markedAt < :cutoff " +
           "AND (j.lockExpiresAt IS NULL OR j.lockExpiresAt < :now) " +
           "AND (j.nextRetryAt IS NULL OR j.nextRetryAt < :now)")
    List<AiAnalysisJob> findProcessableJobs(
            @Param("cutoff") OffsetDateTime cutoff,
            @Param("now") OffsetDateTime now,
            Pageable pageable);

    /**
     * Release stale processing locks on scheduler startup.
     * Prevents jobs stuck in PROCESSING after a crash from blocking indefinitely.
     */
    @Modifying
    @Query("UPDATE AiAnalysisJob j SET j.status = 'PENDING', j.lockExpiresAt = NULL " +
           "WHERE j.status = 'PROCESSING' AND j.lockExpiresAt < :now")
    int releaseExpiredLocks(@Param("now") OffsetDateTime now);

    /**
     * Purge completed and failed jobs past their retention window.
     * COMPLETED: 24h, FAILED: 7d — call from a separate cleanup scheduler.
     */
    @Modifying
    @Query("DELETE FROM AiAnalysisJob j WHERE " +
           "(j.status = 'COMPLETED' AND j.completedAt < :completedCutoff) OR " +
           "(j.status = 'FAILED'    AND j.completedAt < :failedCutoff)")
    int purgeExpiredJobs(
            @Param("completedCutoff") OffsetDateTime completedCutoff,
            @Param("failedCutoff") OffsetDateTime failedCutoff);
}
