package com.healthcare.dto;

import java.util.List;

public record EncounterPageResponse(
        long total,
        int page,
        int size,
        List<EncounterSummaryResponse> encounters) {
}
