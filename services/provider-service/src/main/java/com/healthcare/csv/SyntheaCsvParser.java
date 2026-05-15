package com.healthcare.csv;

import static com.healthcare.csv.SyntheaCsvColumns.*;
import com.healthcare.exception.ProviderServiceException;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SyntheaCsvParser {

    private SyntheaCsvParser() {}

    public static List<SyntheaRows.Organization> parseOrganizations(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Organization(
                        uuid(r, ID),      str(r, NAME),    str(r, ADDRESS), str(r, CITY),
                        str(r, STATE),    str(r, ZIP),
                        decimal(r, LAT),  decimal(r, LON),
                        str(r, PHONE),    decimal(r, REVENUE), integer(r, UTILIZATION)))
                .toList();
    }

    public static List<SyntheaRows.Patient> parsePatients(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Patient(
                        uuid(r, ID),
                        date(r, BIRTHDATE),   date(r, DEATHDATE),
                        str(r, SSN),          str(r, DRIVERS),     str(r, PASSPORT),
                        str(r, PREFIX),       str(r, FIRST),       str(r, MIDDLE),
                        str(r, LAST),         str(r, SUFFIX),      str(r, MAIDEN),
                        str(r, MARITAL),      str(r, RACE),        str(r, ETHNICITY),
                        str(r, GENDER),       str(r, BIRTHPLACE),
                        str(r, ADDRESS),      str(r, CITY),        str(r, STATE),
                        str(r, COUNTY),       str(r, FIPS),        str(r, ZIP),
                        decimal(r, LAT),      decimal(r, LON),
                        decimal(r, HEALTHCARE_EXPENSES), decimal(r, HEALTHCARE_COVERAGE),
                        integer(r, INCOME)))
                .toList();
    }

    public static List<SyntheaRows.Provider> parseProviders(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Provider(
                        uuid(r, ID),            uuid(r, ORGANIZATION),
                        str(r, NAME),           str(r, GENDER),      str(r, SPECIALITY),
                        integer(r, ENCOUNTERS), integer(r, PROCEDURES)))
                .toList();
    }

    public static List<SyntheaRows.Encounter> parseEncounters(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Encounter(
                        uuid(r, ID),
                        uuid(r, PATIENT),         uuid(r, ORGANIZATION), uuid(r, PROVIDER),
                        str(r, PAYER),
                        offsetDt(r, START),       offsetDt(r, STOP),
                        str(r, ENCOUNTERCLASS),   str(r, CODE),          str(r, DESCRIPTION),
                        decimal(r, BASE_ENCOUNTER_COST), decimal(r, TOTAL_CLAIM_COST),
                        decimal(r, PAYER_COVERAGE),
                        str(r, REASONCODE),       str(r, REASONDESCRIPTION)))
                .toList();
    }

    public static List<SyntheaRows.Condition> parseConditions(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Condition(
                        uuid(r, PATIENT),  uuid(r, ENCOUNTER), str(r, CODE),
                        date(r, START),    date(r, STOP),
                        str(r, SYSTEM),    str(r, DESCRIPTION)))
                .toList();
    }

    public static List<SyntheaRows.Allergy> parseAllergies(MultipartFile file) {
        return parse(file).stream()
                .map(r -> new SyntheaRows.Allergy(
                        uuid(r, PATIENT),      uuid(r, ENCOUNTER),    str(r, CODE),
                        date(r, START),        date(r, STOP),
                        str(r, SYSTEM),        str(r, DESCRIPTION),
                        str(r, TYPE),          str(r, CATEGORY),
                        str(r, REACTION1),     str(r, DESCRIPTION1),  str(r, SEVERITY1),
                        str(r, REACTION2),     str(r, DESCRIPTION2),  str(r, SEVERITY2)))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static List<Map<String, String>> parse(MultipartFile file) {
        try (var reader = new CSVReaderHeaderAware(new InputStreamReader(file.getInputStream()))) {
            List<Map<String, String>> rows = new ArrayList<>();
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                rows.add(row);
            }
            return rows;
        } catch (IOException | CsvValidationException e) {
            throw new ProviderServiceException(HttpStatus.BAD_REQUEST,
                    ProviderServiceException.INVALID_CSV,
                    "Failed to parse CSV: " + e.getMessage());
        }
    }

    private static String str(Map<String, String> row, String col) {
        String v = row.get(col);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static UUID uuid(Map<String, String> row, String col) {
        String v = str(row, col);
        return v == null ? null : UUID.fromString(v);
    }

    private static LocalDate date(Map<String, String> row, String col) {
        String v = str(row, col);
        return v == null ? null : LocalDate.parse(v);
    }

    private static OffsetDateTime offsetDt(Map<String, String> row, String col) {
        String v = str(row, col);
        return v == null ? null : OffsetDateTime.parse(v);
    }

    private static BigDecimal decimal(Map<String, String> row, String col) {
        String v = str(row, col);
        return v == null ? null : new BigDecimal(v);
    }

    private static Integer integer(Map<String, String> row, String col) {
        String v = str(row, col);
        return v == null ? null : Integer.parseInt(v);
    }
}
