package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * API Request for getting medical history
 *
 * This matches the design document specification for GET /api/patients/medical-history
 * Authentication is handled via JWT token in Authorization header
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMedicalHistoryRequest {

    // JWT token is handled via Authorization header, not in request body
    // This class exists for API consistency and future extensibility

    // Future query parameters can be added here if needed:
    // - startDate: String (YYYY-MM-DD)
    // - endDate: String (YYYY-MM-DD)
    // - recordType: String (DIAGNOSIS, TREATMENT, etc.)
    // - providerId: String
    // - includeReleasedOnly: boolean
    // - page: int
    // - size: int
    // - etc.
}
