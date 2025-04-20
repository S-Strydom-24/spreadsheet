package com.stephanstrydom.spreadsheet.model;

import java.math.BigDecimal;

public class SpreadsheetFunction {
    private BigDecimal derivedValue;
    private String derivedValueTextRepresentation;
    private final String functionText;
    private final int columnIndex;
    private final long rowIndex;

    public SpreadsheetFunction(BigDecimal value, int columnIndex, long rowIndex,String functionText) {
        this.derivedValue = value;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.functionText = functionText;
    }

    public String getFunctionText() {
        return functionText;
    }

    public String getDerivedValueTextRepresentation() {
        return derivedValueTextRepresentation;
    }

    public void setDerivedValueTextRepresentation(String derivedValueTextRepresentation) {
        this.derivedValueTextRepresentation = derivedValueTextRepresentation;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public BigDecimal getDerivedValue() {
        return derivedValue;
    }

    public void setDerivedValue(BigDecimal derivedValue) {
        this.derivedValue = derivedValue;
    }

    public long getRowIndex() {
        return rowIndex;
    }
}
