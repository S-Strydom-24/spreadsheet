package com.stephanstrydom.spreadsheet.rendering;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;
import com.stephanstrydom.spreadsheet.util.CsvReader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.stephanstrydom.spreadsheet.Spreadsheet.*;
import static com.stephanstrydom.spreadsheet.util.SpreadsheetUtils.*;

public class SimpleSpreadsheetRenderer extends CsvReader implements SpreadsheetRenderer {

    private long rowCount = 1;
    private int columnCount = 1;
    private boolean escapedText = false;
    private long columnCharacterCount = 0;
    private Map<String, SpreadsheetFunction> knownValues;
    private CsvMetaData metaData;
    private Writer writer;

    @Override
    public void render(Reader csvReader, Writer writer, CsvMetaData metaData, Map<String, SpreadsheetFunction> knownValues) throws IOException {
        try (writer) {
            this.writer = writer;
            this.metaData = metaData;
            this.knownValues = knownValues;
            read(csvReader);
            renderEmptyTrailingCells();
            if(DEBUG_MODE) System.out.println();
        }
    }

    @Override
    protected void handleDelimiter() throws IOException {
        if(escapedText) {//it's inside an escaped block
            if(DEBUG_MODE) System.out.print(DELIMITER);
            writer.write(DELIMITER);
            columnCharacterCount++;
            return;
        }
        padWithTrailingSpaces();
        columnCount++;
        if(DEBUG_MODE) System.out.print('|');
        writer.write('|');
        columnCharacterCount = 0;
    }

    @Override
    protected void handleNewline() throws IOException {
        renderEmptyTrailingCells();
        padWithTrailingSpaces();

        if(DEBUG_MODE) System.out.println();
        writer.write("\r\n");

        rowCount++;
        columnCount = 1;
        columnCharacterCount = 0;
    }

    @Override
    protected void handleQuote() throws IOException {
        if(isEscapedQuote(getBufferedReader())) {
            columnCharacterCount++;
            if(DEBUG_MODE) System.out.print('"');
            writer.write('"');
        }
        else {
            escapedText = !escapedText;
        }
    }

    @Override
    protected void handleHash() throws IOException {
        if(escapedText) return;
        String function = readUpTo(getBufferedReader(), List.of(DELIMITER, '\r', '\n'), false).trim();
        if(function.equals(LINE_FUNCTION)) { //draw the horizontal line
            for (int i = 0; i < metaData.columnWidths().get(columnCount-1); i++) {
                if(DEBUG_MODE) System.out.print('-');
                writer.write('-');
                columnCharacterCount++;
            }
        }
        else {
            String coordinate = convertToCoordinate(rowCount, columnCount);
            if (knownValues.containsKey(coordinate)) {
                String text = pad(knownValues.get(coordinate).getDerivedValueTextRepresentation(),
                        metaData.columnWidths().get(columnCount - 1), " ", false);
                if(DEBUG_MODE) System.out.print(text);
                writer.write(text);
                columnCharacterCount += text.length();
            } else {
                String functionText = "#"+function;
                if(DEBUG_MODE) System.out.print(functionText);
                writer.write(functionText);
                columnCharacterCount += functionText.length();
            }
        }
    }

    @Override
    protected void handleCellCharacter(int character) throws IOException {
        List<Character> stopCharacter = List.of(DELIMITER, '"', '\r', '\n');
        if(escapedText) stopCharacter = List.of('"'); //read the whole escaped block
        String text = (char) character+readUpTo(getBufferedReader(), stopCharacter, false);
        text = pad(text, metaData.columnWidths().get(columnCount-1), " ", true);
        columnCharacterCount += text.length();
        if(DEBUG_MODE) System.out.print(text);
        writer.write(text);
    }

    @Override
    protected void postRead() { /* unused */ }

    private void padWithTrailingSpaces() throws IOException {
        while(columnCharacterCount+1 <= metaData.columnWidths().get(columnCount-1)) {
            if(DEBUG_MODE) System.out.print(' ');
            writer.write(' ');
            columnCharacterCount++;
        }
    }

    private static String pad(String stringToPad, long padding, String pad, boolean onlyPadNumbers) {
        StringBuilder output = new StringBuilder(stringToPad);
        Optional<BigDecimal> numberValue = toBigDecimal(stringToPad);
        if(numberValue.isPresent()) {
            output = new StringBuilder(getNumberFormat().format(numberValue.get()));
        } else if(onlyPadNumbers) {
            return stringToPad;
        }
        while(output.length() < padding) {
            if (numberValue.isPresent()) {//pad with leading spaces
                output.insert(0, pad);
            } else { //pad with trailing spaces
                output.append(pad);
            }
        }
        return output.toString();
    }

    private void renderEmptyTrailingCells() throws IOException {
        while(columnCount < metaData.columnWidths().size()) {
            while(columnCharacterCount < metaData.columnWidths().get(columnCount-1)) {
                columnCharacterCount++;
                if(DEBUG_MODE) System.out.print(' ');
                writer.write(' ');
            }
            if(DEBUG_MODE) System.out.print('|');
            writer.write('|');
            columnCharacterCount = 0;
            columnCount++;
        }
    }
}
