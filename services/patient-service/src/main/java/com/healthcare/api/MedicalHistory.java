package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

/**
 * Medical history information for patient API
 *
 * Represents structured medical history data with specific fields for
 * conditions, surgeries, and hospitalizations. All fields are optional
 * to support partial updates.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_CONDITIONS)
    @Size(max = 50, message = "Cannot have more than 50 medical conditions")
    private List<String> conditions;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_SURGERIES)
    @Size(max = 30, message = "Cannot have more than 30 surgical procedures")
    private List<String> surgeries;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_HOSPITALIZATIONS)
    @Size(max = 20, message = "Cannot have more than 20 hospitalizations")
    private List<String> hospitalizations;

    /**
     * Check if medical history is empty (all fields are null or empty)
     * @return true if all fields are null or empty
     */
    public boolean isEmpty() {
        return (conditions == null || conditions.isEmpty()) &&
               (surgeries == null || surgeries.isEmpty()) &&
               (hospitalizations == null || hospitalizations.isEmpty());
    }

    /**
     * Check if any field was provided (not null)
     * @return true if at least one field is not null
     */
    public boolean hasAnyField() {
        return conditions != null || surgeries != null || hospitalizations != null;
    }
}
