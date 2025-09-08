package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * API Request for getting patient profile
 *
 * This matches the design document specification for GET /api/patients/profile
 * Authentication is handled via JWT token in Authorization header
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
public class GetPatientProfileRequest {

    // JWT token is handled via Authorization header, not in request body
    // This class exists for API consistency and future extensibility

    // Future query parameters can be added here if needed:
    // - includeMedicalHistory: boolean
    // - includeAllergies: boolean
    // - includeInsurance: boolean
    // - dateRange: String
    // - etc.
}
