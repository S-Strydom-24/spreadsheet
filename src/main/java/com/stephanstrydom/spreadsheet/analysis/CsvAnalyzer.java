package com.stephanstrydom.spreadsheet.analysis;

import com.stephanstrydom.spreadsheet.model.CsvMetaData;

import java.io.IOException;
import java.io.Reader;

public interface CsvAnalyzer {
    CsvMetaData analyze(Reader csvReader) throws IOException;
}
