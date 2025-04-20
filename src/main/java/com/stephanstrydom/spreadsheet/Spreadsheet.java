package com.stephanstrydom.spreadsheet;

import com.stephanstrydom.spreadsheet.analysis.CsvAnalyzer;
import com.stephanstrydom.spreadsheet.analysis.SimpleCsvAnalyzer;
import com.stephanstrydom.spreadsheet.collection.CsvCollector;
import com.stephanstrydom.spreadsheet.collection.SimpleCsvCollector;
import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.FailedValidation;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;
import com.stephanstrydom.spreadsheet.rendering.SpreadsheetRenderer;
import com.stephanstrydom.spreadsheet.rendering.SimpleSpreadsheetRenderer;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Spreadsheet {

    public static final boolean DEBUG_MODE = false;

    public static final boolean CHECK_COLUMN_COUNT_CONSISTENCY = true;
    public static final int VALIDATION_FAIL_LIMIT = 20;
    public static final char DELIMITER = ',';
    public static final List<String> FUNCTION_NAMES = List.of("sum","prod");
    public static final String LINE_FUNCTION = "hl";
    public static final int MAX_FUNCTION_SIZE = 100; //in chars

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Application requires two arguments. An input file path and an output file path.");
            System.exit(1);
        }

        final Path input = Path.of(args[0]);
        final Path output = Path.of(args[1]);

        if(!input.toFile().exists()) {
            System.err.println("File not found: "+input.toFile().getAbsolutePath());
            System.exit(1);
        }
        if(output.toFile().exists()) {
            System.err.println("File already exists: "+output.toFile().getAbsolutePath());
            System.exit(1);
        }

        final CsvAnalyzer analyzer = new SimpleCsvAnalyzer();
        final CsvCollector collector = new SimpleCsvCollector();
        final SpreadsheetRenderer renderer = new SimpleSpreadsheetRenderer();

        String step = "Collecting metadata for CSV file";
        try {
            CsvMetaData metaData = analyzer.analyze(new FileReader(input.toFile()));

            String errors = metaData.failedValidations().stream()
                    .filter(failedValidation -> failedValidation.severity().equals(FailedValidation.Severity.ERROR))
                    .map(failedValidation -> FailedValidation.Severity.ERROR.name()+": "+failedValidation.description()+" line:"+failedValidation.line())
                    .collect(Collectors.joining("\n"));
            if(errors.length() > 0) {
                System.err.println(errors);
                return;
            }

            step = "Calculating derived values";
            Map<String, SpreadsheetFunction> derivedValues = collector.collect(new FileReader(input.toFile()), metaData);

            String warnings = metaData.failedValidations().stream()
                    .filter(failedValidation -> failedValidation.severity().equals(FailedValidation.Severity.WARNING))
                    .map(failedValidation -> FailedValidation.Severity.WARNING.name()+": "+failedValidation.description()+" line:"+failedValidation.line())
                    .collect(Collectors.joining("\n"));
            if(warnings.length() > 0) {
                System.out.println(warnings);
            }

            step = "Rendering output file";
            renderer.render(new FileReader(input.toFile()), new FileWriter(output.toFile()), metaData, derivedValues);
        } catch (Exception e) {
            if(DEBUG_MODE) e.printStackTrace();
            else System.err.println(step+": "+e.getMessage());
            System.exit(1);
        }
    }
}
