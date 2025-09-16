package com.healthcare.dao;

import com.healthcare.entity.Appointment;
import com.healthcare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for Appointment entity
 * Handles database operations for appointments
 */
@Repository
public interface AppointmentDao extends JpaRepository<Appointment, UUID> {

    /**
     * Find appointments by patient ID
     * Used to get all appointments for a specific patient
     *
     * @param patientId The patient ID
     * @return List of appointments for the patient
     */
    List<Appointment> findByPatientId(UUID patientId);

    /**
     * Find appointments by provider ID
     * Used to get all appointments for a specific provider
     *
     * @param providerId The provider ID
     * @return List of appointments for the provider
     */
    List<Appointment> findByProviderId(UUID providerId);

    /**
     * Find available appointment slots for a provider
     * Used for appointment booking - finds slots that are available
     *
     * @param providerId The provider ID
     * @param start The start of the time range
     * @param end The end of the time range
     * @return List of available appointment slots
     */
    @Query("SELECT a FROM Appointment a WHERE a.providerId = :providerId " +
           "AND a.scheduledAt BETWEEN :start AND :end " +
           "AND a.status = 'AVAILABLE'")
    List<Appointment> findAvailableSlots(@Param("providerId") UUID providerId,
                                        @Param("start") OffsetDateTime start,
                                        @Param("end") OffsetDateTime end);

    /**
     * Create a new appointment
     * Saves the appointment entity to the database
     *
     * @param appointment The appointment entity to create
     * @return The created appointment entity
     */
    default Appointment create(Appointment appointment) {
        return save(appointment);
    }

    /**
     * Update an existing appointment
     * Updates the appointment entity in the database
     *
     * @param appointment The appointment entity to update
     * @return The updated appointment entity
     */
    default Appointment update(Appointment appointment) {
        return save(appointment);
    }
}
