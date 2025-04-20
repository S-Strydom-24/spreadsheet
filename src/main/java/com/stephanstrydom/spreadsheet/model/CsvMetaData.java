package com.stephanstrydom.spreadsheet.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CsvMetaData(
        Map<Integer,Long> columnWidths,
        List<FailedValidation> failedValidations,
        Set<String> referencedCells
) {}
