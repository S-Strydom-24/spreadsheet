package com.stephanstrydom.spreadsheet.analysis;

import com.stephanstrydom.spreadsheet.common.TestComponents;
import com.stephanstrydom.spreadsheet.model.CsvMetaData;
import com.stephanstrydom.spreadsheet.model.FailedValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SimpleCsvAnalyzerTests {

    private final static String CSV = """
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

    @Test
    void testAnalysis() {
        try {
            CsvMetaData metaData = TestComponents.getMetaData();
            Assertions.assertEquals(22, metaData.failedValidations().size());
            Assertions.assertEquals("Column count seems inconsistent.", metaData.failedValidations().get(0).description());
            Assertions.assertEquals(3, metaData.failedValidations().get(1).line());
            Assertions.assertEquals(FailedValidation.Severity.WARNING, metaData.failedValidations().get(2).severity());
            Assertions.assertEquals(6, metaData.failedValidations().get(3).line());
            Assertions.assertEquals("Malformed function.", metaData.failedValidations().get(3).description());
            Assertions.assertEquals(FailedValidation.Severity.ERROR, metaData.failedValidations().get(3).severity());

            Assertions.assertEquals(2, metaData.referencedCells().size());
            Assertions.assertEquals(8, metaData.columnWidths().size());
            Assertions.assertEquals(23, metaData.columnWidths().get(0));
            Assertions.assertEquals(16, metaData.columnWidths().get(1));
            Assertions.assertEquals(45, metaData.columnWidths().get(2));
            Assertions.assertEquals(4, metaData.columnWidths().get(3));
            Assertions.assertEquals(0, metaData.columnWidths().get(4));
            Assertions.assertEquals(0, metaData.columnWidths().get(5));
            Assertions.assertEquals(0, metaData.columnWidths().get(6));
            Assertions.assertEquals(0, metaData.columnWidths().get(7));

            Assertions.assertTrue(metaData.referencedCells().contains("B4"));
            Assertions.assertTrue(metaData.referencedCells().contains("A4"));
            Assertions.assertFalse(metaData.referencedCells().contains("D4"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
