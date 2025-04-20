package com.stephanstrydom.spreadsheet.analysis;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.FailedValidation;
import com.stephanstrydom.spreadsheet.util.CsvReader;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.stephanstrydom.spreadsheet.Spreadsheet.*;
import static com.stephanstrydom.spreadsheet.util.SpreadsheetUtils.isEscapedQuote;
import static com.stephanstrydom.spreadsheet.util.SpreadsheetUtils.readUpTo;

public class SimpleCsvAnalyzer extends CsvReader implements CsvAnalyzer {

    private long rowCount = 0;
    private int columnCount = 0;
    private int previousColumnCount = 0;
    private long columnCharacterCount = 0;

    private final Map<Integer, Long> largestColumnWidths = new HashMap<>(); //column index and max width
    private final Set<String> referencedCells = new HashSet<>(); //cells that are referenced by functions
    private final List<FailedValidation> failedValidations = new ArrayList<>();

    boolean escapedText = false;

    @Override
    public CsvMetaData analyze(Reader csvReader) throws IOException {
        read(csvReader);
        return new CsvMetaData(
                largestColumnWidths,
                failedValidations,
                referencedCells);
    }

    @Override
    protected void handleDelimiter() {
        if(escapedText) {//we're in an escaped block, ignore delimiters
            columnCharacterCount++;
            return;
        }
        columnCharacterCount = 0;
        columnCount++;
    }

    @Override
    protected void handleNewline() {
        expandColumnIfNeeded();
        columnCharacterCount = 0;
        if(CHECK_COLUMN_COUNT_CONSISTENCY)
            checkColumnCountConsistency(previousColumnCount, columnCount, rowCount, failedValidations);
        previousColumnCount = columnCount;
        columnCount = 0;
        rowCount++;
    }

    @Override
    protected void handleHash() throws IOException {
        if(escapedText) return; //we're in an escaped block, ignore functions
        String function = readUpTo(getBufferedReader(), List.of(DELIMITER, '\n', '\r'), false).trim();
        if (validateFunction(function, rowCount, failedValidations) && !function.equals(LINE_FUNCTION)) {
            String trimmedCommand = function.substring(1, function.length() - 1); //remove brackets
            referencedCells.addAll(Arrays.stream(trimmedCommand.split(" ")) //add to the referenced cells
                    .filter(functionElement -> !FUNCTION_NAMES.contains(functionElement))
                    .toList());
            columnCharacterCount = Math.max(1,columnCharacterCount);
        }
    }

    @Override
    protected void handleQuote() throws IOException {
        if(isEscapedQuote(getBufferedReader())) {
            columnCharacterCount++;
        }
        else {//We're entering an escaped block
            escapedText = !escapedText;
        }
    }

    @Override
    protected void handleCellCharacter(int character) {
        columnCharacterCount++;
    }

    @Override
    protected void postRead() {
        expandColumnIfNeeded();
        if(failedValidations.size() > VALIDATION_FAIL_LIMIT) {
            failedValidations.add(new FailedValidation(
                    rowCount, "Too many validation errors. Aborting analysis.",
                    FailedValidation.Severity.ERROR));
            stopReading();
        }
    }

    private void expandColumnIfNeeded() {
        if (largestColumnWidths.getOrDefault(columnCount, 0L) <= columnCharacterCount)
            largestColumnWidths.put(columnCount, columnCharacterCount);
    }

    private static void checkColumnCountConsistency(int previousColumnCount, int columnCount,
                                                    long rowIndex, List<FailedValidation> failedValidations) {
        if(previousColumnCount != 0 && previousColumnCount != columnCount) {
            failedValidations.add(new FailedValidation(
                    rowIndex+1, "Column count seems inconsistent.", FailedValidation.Severity.WARNING));
        }
    }

    private static boolean validateFunction(String function, long rowCount,
                                         List<FailedValidation> failedValidations) {
        if (function.equals(LINE_FUNCTION)) return true; //We don't need to analyse further
        int openBracketCount = function.length() - function.replace("(", "").length();
        int closeBracketCount = function.length() - function.replace(")", "").length();
        if(openBracketCount != 1 || closeBracketCount != 1 || !function.startsWith("(") || !function.endsWith(")")) {
            failedValidations.add(new FailedValidation(rowCount+1, "Malformed function.", FailedValidation.Severity.ERROR));
            return false;
        }
        return true;
    }
}
