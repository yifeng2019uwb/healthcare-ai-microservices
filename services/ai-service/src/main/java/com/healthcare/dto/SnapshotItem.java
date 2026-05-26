package com.healthcare.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SnapshotItem(
        String code,
        String description,
        String startDate,
        String stopDate,
        String encounterDate) {
}
