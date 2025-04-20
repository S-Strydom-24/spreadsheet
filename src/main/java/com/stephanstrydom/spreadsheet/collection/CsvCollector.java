package com.stephanstrydom.spreadsheet.collection;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public interface CsvCollector {
    Map<String, SpreadsheetFunction> collect(Reader csvReader, CsvMetaData metaData) throws IOException;
}
