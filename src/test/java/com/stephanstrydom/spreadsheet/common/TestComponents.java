package com.stephanstrydom.spreadsheet.common;

import com.stephanstrydom.spreadsheet.analysis.CsvAnalyzer;
import com.stephanstrydom.spreadsheet.analysis.SimpleCsvAnalyzer;
import com.stephanstrydom.spreadsheet.collection.CsvCollector;
import com.stephanstrydom.spreadsheet.collection.SimpleCsvCollector;
import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;
import com.stephanstrydom.spreadsheet.rendering.SimpleSpreadsheetRenderer;
import com.stephanstrydom.spreadsheet.rendering.SpreadsheetRenderer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class TestComponents {

    private final static String CSV_WITH_MANY_ERRORS = """
            one,two,three,four
            one,two,three
            one,,,four
            1,2,3,4
            #hl,#(sum A4 B4),34
            #(sum D4,hello,byeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
            "escaped commas,,,,,,,,,", escaped quote \"\"\", blah
            #(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4
            #(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4
            #(sum D4,#(sum D4,#(sum D4,#(sum D4,#(sum D4""";

    private final static String CSV_WITH_NO_ERRORS = """
            one,two,three,four
            one,two,three
            one,,,#(prod B5 A4),
            1,2,3,4,#(prod B5 A9),
            #hl,#(sum A4 B4),34
            ,hello,byeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
            "escaped commas,,,,,,,,,", escaped quote \"\"\", blah,""";

    public static CsvMetaData getMetaData() throws IOException {
        CsvAnalyzer analyzer = new SimpleCsvAnalyzer();
        return analyzer.analyze(new StringReader(CSV_WITH_MANY_ERRORS));
    }

    public static Map<String, SpreadsheetFunction> getKnownValues() throws IOException {
        CsvAnalyzer analyzer = new SimpleCsvAnalyzer();
        CsvMetaData metaData = analyzer.analyze(new StringReader(CSV_WITH_MANY_ERRORS));
        CsvCollector collector = new SimpleCsvCollector();
        return collector.collect(new StringReader(CSV_WITH_NO_ERRORS), metaData);
    }

    public static String getRendering() throws IOException {
        CsvAnalyzer analyzer = new SimpleCsvAnalyzer();
        CsvMetaData metaData = analyzer.analyze(new StringReader(CSV_WITH_MANY_ERRORS));
        CsvCollector collector = new SimpleCsvCollector();
        Map<String, SpreadsheetFunction> knownValues = collector.collect(new StringReader(CSV_WITH_NO_ERRORS), metaData);
        SpreadsheetRenderer renderer = new SimpleSpreadsheetRenderer();
        StringWriter writer = new StringWriter();
        renderer.render(new StringReader(CSV_WITH_NO_ERRORS),
                writer,
                metaData,
                knownValues);
        return writer.toString();
    }
}
