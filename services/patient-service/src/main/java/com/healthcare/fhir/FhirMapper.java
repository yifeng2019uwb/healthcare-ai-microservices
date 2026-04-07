package com.healthcare.fhir;

import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.EncounterResponse;
import com.healthcare.dto.PageResponse;
import com.healthcare.dto.PatientProfileResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Maps internal DTOs to FHIR R4 resource shapes (CMS-9115-F / HL7 FHIR R4).
 *
 * Returns plain Map<String, Object> — no HAPI dependency required.
 * Resources covered: Patient, Encounter, Condition, AllergyIntolerance.
 *
 * Demonstrates knowledge of:
 *   - CMS Interoperability and Patient Access Rule (CMS-9115-F)
 *   - FHIR R4 resource structure and terminology bindings
 *   - SNOMED CT / ICD-10 coding systems
 */
public final class FhirMapper {

    // FHIR terminology system URIs
    private static final String SYSTEM_ACT_CODE  =
            "http://terminology.hl7.org/CodeSystem/v3-ActCode";
    private static final String SYSTEM_CONDITION_CLINICAL =
            "http://terminology.hl7.org/CodeSystem/condition-clinical";
    private static final String SYSTEM_ALLERGY_CLINICAL =
            "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical";
    private static final String SYSTEM_SNOMED = "http://snomed.info/sct";

    private FhirMapper() {}

    // -------------------------------------------------------------------------
    // Patient resource
    // -------------------------------------------------------------------------

    /**
     * Maps PatientProfileResponse → FHIR R4 Patient resource.
     *
     * @see <a href="https://hl7.org/fhir/R4/patient.html">FHIR R4 Patient</a>
     */
    public static Map<String, Object> toPatient(PatientProfileResponse p) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Patient");
        resource.put("id", p.id() != null ? p.id().toString() : null);

        // Identifier — MRN as internal system identifier
        resource.put("identifier", List.of(
                coding("urn:oid:2.16.840.1.113883.4.6", p.mrn())
        ));

        // Human name
        Map<String, Object> name = new LinkedHashMap<>();
        name.put("family", p.lastName());
        List<String> given = new ArrayList<>();
        if (p.firstName() != null) given.add(p.firstName());
        if (p.middleName() != null) given.add(p.middleName());
        name.put("given", given);
        if (p.prefix() != null) name.put("prefix", List.of(p.prefix()));
        if (p.suffix() != null) name.put("suffix", List.of(p.suffix()));
        resource.put("name", List.of(name));

        // Gender — FHIR uses "male"/"female"/"other"/"unknown"
        if (p.gender() != null) {
            resource.put("gender", fhirGender(p.gender().name()));
        }

        resource.put("birthDate", p.birthdate());

        // Telecom — phone
        if (p.phone() != null) {
            resource.put("telecom", List.of(
                    Map.of("system", "phone", "value", p.phone())
            ));
        }

        // Address
        if (p.address() != null || p.city() != null) {
            Map<String, Object> address = new LinkedHashMap<>();
            if (p.address() != null) address.put("line", List.of(p.address()));
            if (p.city()    != null) address.put("city", p.city());
            if (p.state()   != null) address.put("state", p.state());
            if (p.zip()     != null) address.put("postalCode", p.zip());
            resource.put("address", List.of(address));
        }

        return resource;
    }

    // -------------------------------------------------------------------------
    // Encounter Bundle
    // -------------------------------------------------------------------------

    /**
     * Maps PageResponse<EncounterResponse> → FHIR R4 Bundle (searchset) of Encounter resources.
     *
     * @see <a href="https://hl7.org/fhir/R4/encounter.html">FHIR R4 Encounter</a>
     */
    public static Map<String, Object> toEncounterBundle(
            UUID patientId, PageResponse<EncounterResponse> page) {

        List<Map<String, Object>> entries = page.data().stream()
                .map(e -> entry(toEncounter(patientId, e)))
                .toList();

        return bundle(page.total(), entries);
    }

    private static Map<String, Object> toEncounter(UUID patientId, EncounterResponse e) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Encounter");
        resource.put("id", e.id() != null ? e.id().toString() : null);

        // Status — map EncounterStatus enum to FHIR values
        resource.put("status", e.status() != null
                ? fhirEncounterStatus(e.status().name()) : "unknown");

        // Class — FHIR uses V3 ActCode
        if (e.encounterClass() != null) {
            resource.put("class", Map.of(
                    "system", SYSTEM_ACT_CODE,
                    "code", e.encounterClass().toUpperCase()
            ));
        }

        // Type — use SNOMED system if available
        if (e.code() != null || e.description() != null) {
            resource.put("type", List.of(Map.of(
                    "coding", List.of(codedConcept(SYSTEM_SNOMED, e.code(), e.description()))
            )));
        }

        // Subject reference
        resource.put("subject", reference("Patient", patientId));

        // Period
        if (e.startTime() != null || e.stopTime() != null) {
            Map<String, Object> period = new LinkedHashMap<>();
            if (e.startTime() != null) period.put("start", e.startTime().toString());
            if (e.stopTime()  != null) period.put("end",   e.stopTime().toString());
            resource.put("period", period);
        }

        // Reason code
        if (e.reasonCode() != null || e.reasonDesc() != null) {
            resource.put("reasonCode", List.of(Map.of(
                    "coding", List.of(codedConcept(SYSTEM_SNOMED, e.reasonCode(), e.reasonDesc()))
            )));
        }

        return resource;
    }

    // -------------------------------------------------------------------------
    // Condition Bundle
    // -------------------------------------------------------------------------

    /**
     * Maps List<ConditionResponse> → FHIR R4 Bundle (searchset) of Condition resources.
     *
     * @see <a href="https://hl7.org/fhir/R4/condition.html">FHIR R4 Condition</a>
     */
    public static Map<String, Object> toConditionBundle(
            UUID patientId, List<ConditionResponse> conditions) {

        List<Map<String, Object>> entries = conditions.stream()
                .map(c -> entry(toCondition(patientId, c)))
                .toList();

        return bundle(conditions.size(), entries);
    }

    private static Map<String, Object> toCondition(UUID patientId, ConditionResponse c) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Condition");

        // Clinical status — active / resolved
        String statusCode = c.ongoing() ? "active" : "resolved";
        resource.put("clinicalStatus", Map.of(
                "coding", List.of(Map.of(
                        "system", SYSTEM_CONDITION_CLINICAL,
                        "code", statusCode
                ))
        ));

        // Code
        resource.put("code", Map.of(
                "coding", List.of(codedConcept(c.system(), c.code(), c.description()))
        ));

        resource.put("subject", reference("Patient", patientId));

        if (c.startDate() != null) resource.put("onsetDateTime", c.startDate().toString());
        if (c.stopDate()  != null) resource.put("abatementDateTime", c.stopDate().toString());

        return resource;
    }

    // -------------------------------------------------------------------------
    // AllergyIntolerance Bundle
    // -------------------------------------------------------------------------

    /**
     * Maps List<AllergyResponse> → FHIR R4 Bundle (searchset) of AllergyIntolerance resources.
     *
     * @see <a href="https://hl7.org/fhir/R4/allergyintolerance.html">FHIR R4 AllergyIntolerance</a>
     */
    public static Map<String, Object> toAllergyBundle(
            UUID patientId, List<AllergyResponse> allergies) {

        List<Map<String, Object>> entries = allergies.stream()
                .map(a -> entry(toAllergy(patientId, a)))
                .toList();

        return bundle(allergies.size(), entries);
    }

    private static Map<String, Object> toAllergy(UUID patientId, AllergyResponse a) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "AllergyIntolerance");

        // Clinical status
        String statusCode = a.active() ? "active" : "inactive";
        resource.put("clinicalStatus", Map.of(
                "coding", List.of(Map.of(
                        "system", SYSTEM_ALLERGY_CLINICAL,
                        "code", statusCode
                ))
        ));

        // Code
        resource.put("code", Map.of(
                "coding", List.of(codedConcept(a.system(), a.code(), a.description()))
        ));

        resource.put("patient", reference("Patient", patientId));

        if (a.startDate() != null) resource.put("onsetDateTime", a.startDate().toString());

        // Reactions
        List<Map<String, Object>> reactions = new ArrayList<>();
        addReaction(reactions, a.reaction1(), a.description1(), a.severity1());
        addReaction(reactions, a.reaction2(), a.description2(), a.severity2());
        if (!reactions.isEmpty()) resource.put("reaction", reactions);

        return resource;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Map<String, Object> entry(Map<String, Object> resource) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("resource", resource);
        return m;
    }

    private static Map<String, Object> bundle(long total, List<Map<String, Object>> entries) {
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "searchset");
        bundle.put("total", total);
        bundle.put("entry", entries);
        return bundle;
    }

    private static Map<String, Object> reference(String resourceType, UUID id) {
        return Map.of("reference", resourceType + "/" + (id != null ? id.toString() : "unknown"));
    }

    private static Map<String, Object> coding(String system, String value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("system", system);
        m.put("value", value);
        return m;
    }

    private static Map<String, Object> codedConcept(String system, String code, String display) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (system  != null) m.put("system", system);
        if (code    != null) m.put("code", code);
        if (display != null) m.put("display", display);
        return m;
    }

    private static void addReaction(List<Map<String, Object>> reactions,
                                    String reaction, String description, String severity) {
        if (reaction == null && description == null) return;
        Map<String, Object> r = new LinkedHashMap<>();
        String display = description != null ? description : reaction;
        r.put("manifestation", List.of(Map.of(
                "coding", List.of(Map.of("display", display))
        )));
        if (severity != null) r.put("severity", severity.toLowerCase());
        reactions.add(r);
    }

    private static String fhirGender(String gender) {
        if (gender == null) return "unknown";
        return switch (gender.toUpperCase()) {
            case "M", "MALE"   -> "male";
            case "F", "FEMALE" -> "female";
            default            -> "unknown";
        };
    }

    private static String fhirEncounterStatus(String status) {
        if (status == null) return "unknown";
        return switch (status.toUpperCase()) {
            case "FINISHED", "COMPLETED" -> "finished";
            case "IN_PROGRESS"           -> "in-progress";
            case "PLANNED"               -> "planned";
            case "CANCELLED"             -> "cancelled";
            default                      -> "unknown";
        };
    }
}
