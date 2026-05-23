package com.healthcare.dto;

public record ImportResult(int total, int imported, int skippedDuplicate, int skippedInvalid) {

    public static ImportResult empty() {
        return new ImportResult(0, 0, 0, 0);
    }
}
