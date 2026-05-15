package com.healthcare.csv;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class SyntheaRows {

    private SyntheaRows() {}

    public record Organization(
            UUID id, String name, String address, String city,
            String state, String zip, BigDecimal lat, BigDecimal lon,
            String phone, BigDecimal revenue, Integer utilization) {}

    public record Patient(
            UUID id, LocalDate birthdate, LocalDate deathdate,
            String ssn, String drivers, String passport,
            String prefix, String firstName, String middleName, String lastName,
            String suffix, String maiden, String marital, String race,
            String ethnicity, String gender, String birthplace,
            String address, String city, String state, String county,
            String fips, String zip, BigDecimal lat, BigDecimal lon,
            BigDecimal healthcareExpenses, BigDecimal healthcareCoverage, Integer income) {}

    public record Provider(
            UUID id, UUID organizationId, String name,
            String gender, String speciality,
            Integer encounters, Integer procedures) {}

    public record Encounter(
            UUID id, UUID patientId, UUID organizationId, UUID providerId,
            String payerId, OffsetDateTime startTime, OffsetDateTime stopTime,
            String encounterClass, String code, String description,
            BigDecimal baseCost, BigDecimal totalCost, BigDecimal payerCoverage,
            String reasonCode, String reasonDesc) {}

    public record Condition(
            UUID patientId, UUID encounterId, String code,
            LocalDate startDate, LocalDate stopDate,
            String system, String description) {}

    public record Allergy(
            UUID patientId, UUID encounterId, String code,
            LocalDate startDate, LocalDate stopDate,
            String system, String description, String allergyType, String category,
            String reaction1, String description1, String severity1,
            String reaction2, String description2, String severity2) {}
}
