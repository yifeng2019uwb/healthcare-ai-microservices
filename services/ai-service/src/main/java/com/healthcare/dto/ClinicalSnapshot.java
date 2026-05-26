package com.healthcare.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClinicalSnapshot(
        String triggerCode,
        List<SnapshotItem> conditions,
        List<SnapshotItem> allergies) {
}
