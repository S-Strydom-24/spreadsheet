package com.stephanstrydom.spreadsheet.collection;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.FailedValidation;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;
import com.stephanstrydom.spreadsheet.util.CsvReader;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

import static com.stephanstrydom.spreadsheet.Spreadsheet.*;
import static com.stephanstrydom.spreadsheet.util.SpreadsheetUtils.*;

public class SimpleCsvCollector extends CsvReader implements CsvCollector {

    private long rowCount = 1;
    private int columnCount = 1;
    private boolean escapedText = false;
    private Map<String, SpreadsheetFunction> knownValueCollection = new HashMap<>();
    private Map<String, SpreadsheetFunction> derivedValueCollection = new HashMap<>();
    private CsvMetaData metaData;

    @Override
    public Map<String, SpreadsheetFunction> collect(Reader csvReader, CsvMetaData metaData) throws IOException {
        this.metaData = metaData;
        read(csvReader);
        functionResultCascade(knownValueCollection, derivedValueCollection, metaData);
        return knownValueCollection;
    }

    @Override
    protected void handleDelimiter() {
        if(escapedText) {
            return;
        }
        columnCount++;
    }

    @Override
    protected void handleNewline() {
        rowCount++;
        columnCount = 1;
    }

    @Override
    protected void handleQuote() throws IOException {
        if(!isEscapedQuote(getBufferedReader())) {
            escapedText = !escapedText;
        }
    }

    @Override
    protected void handleHash() throws IOException {
        if(escapedText) return;
        String function = readUpTo(getBufferedReader(), List.of(DELIMITER, '\r', '\n'), false).trim();
        if (!function.equals(LINE_FUNCTION)) {
            String coordinate = convertToCoordinate(rowCount, columnCount);
            derivedValueCollection.put(coordinate,
                    new SpreadsheetFunction(null, columnCount-1, rowCount,"#"+function.trim()));
        }
    }

    @Override
    protected void handleCellCharacter(int character) throws IOException {
        List<Character> stopCharacter = List.of(DELIMITER, '"', '\r', '\n');
        if(escapedText) stopCharacter = List.of('"');
        String text = (char) character+readUpTo(getBufferedReader(), stopCharacter, false).trim();
        String coordinate = convertToCoordinate(rowCount, columnCount);
        Optional<BigDecimal> value = toBigDecimal(text);
        if (metaData.referencedCells().contains(coordinate)) { //this cell is used in a function
            if (value.isPresent()) { //... and it's a valid number
                knownValueCollection.put(coordinate, new SpreadsheetFunction(value.get(), columnCount-1, rowCount, text));
                generateTextRepresentationAndExpandColumnIfNeeded(knownValueCollection.get(coordinate), metaData);
            }
        } else {
            value.ifPresent(bigDecimal -> expandColumnIfNeeded(bigDecimal, columnCount - 1, metaData));
        }
    }

    @Override
    protected void postRead() { /* unused */ }

    private static void functionResultCascade(Map<String, SpreadsheetFunction> knownValueCollection,
                                              Map<String, SpreadsheetFunction> derivedValueCollection, CsvMetaData metaData) {
        boolean progressing = true; //functions are being resolved
        while(progressing) { //if an iteration doesn't resolve a function, we might as well stop
            progressing = false;
            Set<String> resolvedDerivedValues = new HashSet<>();
            for (Map.Entry<String, SpreadsheetFunction> derivedValueEntry : derivedValueCollection.entrySet()) {
                String coordinate = derivedValueEntry.getKey();
                SpreadsheetFunction function = derivedValueEntry.getValue();

                Optional<BigDecimal> derivedValue = evaluateFunction(knownValueCollection, function.getFunctionText());
                if (derivedValue.isPresent()) {
                    function.setDerivedValue(derivedValue.get());
                    knownValueCollection.put(coordinate, function);
                    resolvedDerivedValues.add(coordinate);
                    generateTextRepresentationAndExpandColumnIfNeeded(knownValueCollection.get(coordinate), metaData);
                    progressing = true;
                }
            }
            resolvedDerivedValues.forEach(derivedValueCollection::remove);//removed functions we have resolved
        }
        //expand the columns that did not resolve so that they can fit the function text
        for(Map.Entry<String, SpreadsheetFunction> unresolvedFunction : derivedValueCollection.entrySet()) {
            final SpreadsheetFunction function = unresolvedFunction.getValue();
            expandColumnIfNeeded(function.getFunctionText(), function.getColumnIndex(), metaData);
            metaData.failedValidations().add(
                    new FailedValidation(function.getRowIndex(), "Could not resolve function.", FailedValidation.Severity.WARNING));
        }
    }

    private static void generateTextRepresentationAndExpandColumnIfNeeded(SpreadsheetFunction result, CsvMetaData metaData) {
        String textVersion = getNumberFormat().format(result.getDerivedValue());
        result.setDerivedValueTextRepresentation(textVersion);
        expandColumnIfNeeded(textVersion, result.getColumnIndex(), metaData);
    }

    private static void expandColumnIfNeeded(BigDecimal value, int column, CsvMetaData metaData) {
        expandColumnIfNeeded(getNumberFormat().format(value), column, metaData);
    }

    private static void expandColumnIfNeeded(String valueAsText, int column, CsvMetaData metaData) {
        if(valueAsText.length() > metaData.columnWidths().getOrDefault(column,0L)) {
            metaData.columnWidths().put(column, (long) valueAsText.length());
        }
    }

    private static Optional<BigDecimal> evaluateFunction(Map<String, SpreadsheetFunction> valueCollection, String command) {
        String trimmedCommand = command.substring(2, command.length()-1);
        List<String> functionComponents = Arrays.stream(trimmedCommand.split(" "))
                .filter(functionElement -> !FUNCTION_NAMES.contains(functionElement))
                .toList();
        if(functionComponents.stream().allMatch(valueCollection::containsKey)) {//we have the values we need to resolve
            if(trimmedCommand.startsWith("sum")) {
                return Optional.of(functionComponents.stream().map(coordinate ->
                        valueCollection.get(coordinate).getDerivedValue()).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            if(trimmedCommand.startsWith("prod")) {
                return Optional.of(functionComponents.stream().map(coordinate ->
                        valueCollection.get(coordinate).getDerivedValue()).reduce(BigDecimal.ONE, BigDecimal::multiply));
            }
        }
        return Optional.empty();
    }
}
