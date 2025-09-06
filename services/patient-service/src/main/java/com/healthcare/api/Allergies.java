package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Allergies information for patient API
 *
 * Represents structured allergy data with specific fields for
 * medications, foods, and environmental allergies. All fields are optional
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
public class Allergies {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_MEDICATIONS)
    @Size(max = 30, message = "Cannot have more than 30 medication allergies")
    private List<String> medications;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_FOODS)
    @Size(max = 20, message = "Cannot have more than 20 food allergies")
    private List<String> foods;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_ENVIRONMENTAL)
    @Size(max = 15, message = "Cannot have more than 15 environmental allergies")
    private List<String> environmental;

    /**
     * Check if allergies is empty (all fields are null or empty)
     * @return true if all fields are null or empty
     */
    public boolean isEmpty() {
        return (medications == null || medications.isEmpty()) &&
               (foods == null || foods.isEmpty()) &&
               (environmental == null || environmental.isEmpty());
    }

    /**
     * Check if any field was provided (not null)
     * @return true if at least one field is not null
     */
    public boolean hasAnyField() {
        return medications != null || foods != null || environmental != null;
    }
}
