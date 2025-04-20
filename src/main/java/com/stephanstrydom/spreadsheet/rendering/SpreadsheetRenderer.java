package com.stephanstrydom.spreadsheet.rendering;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public interface SpreadsheetRenderer {
    void render(Reader csvReader, Writer writer, CsvMetaData metaData, Map<String, SpreadsheetFunction> knownValues) throws IOException;
}
