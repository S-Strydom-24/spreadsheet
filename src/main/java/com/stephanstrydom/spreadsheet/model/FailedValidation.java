package com.stephanstrydom.spreadsheet.model;

public record FailedValidation(
        long line,
        String description,
        Severity severity
) {
    public enum Severity {
        WARNING,
        ERROR
    }
}
