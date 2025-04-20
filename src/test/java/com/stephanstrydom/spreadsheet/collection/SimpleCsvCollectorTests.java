package com.stephanstrydom.spreadsheet.collection;

import com.stephanstrydom.spreadsheet.common.TestComponents;
import com.stephanstrydom.spreadsheet.model.SpreadsheetFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class SimpleCsvCollectorTests {

    @Test
    void testCollection() throws IOException {
        Map<String, SpreadsheetFunction> knownValues = TestComponents.getKnownValues();
        Assertions.assertEquals(4, knownValues.size());
        Assertions.assertTrue(knownValues.containsKey("B4"));
        Assertions.assertTrue(knownValues.containsKey("A4"));
        Assertions.assertTrue(knownValues.containsKey("B5"));
        Assertions.assertTrue(knownValues.containsKey("D3"));
        Assertions.assertEquals(new BigDecimal(2), knownValues.get("B4").getDerivedValue());
        Assertions.assertEquals("2.0", knownValues.get("B4").getDerivedValueTextRepresentation());
        Assertions.assertEquals("2", knownValues.get("B4").getFunctionText());
        Assertions.assertEquals(1, knownValues.get("B4").getColumnIndex());
        Assertions.assertEquals(4, knownValues.get("B4").getRowIndex());
        Assertions.assertEquals(new BigDecimal(1), knownValues.get("A4").getDerivedValue());
        Assertions.assertEquals("1.0", knownValues.get("A4").getDerivedValueTextRepresentation());
        Assertions.assertEquals("1", knownValues.get("A4").getFunctionText());
        Assertions.assertEquals(0, knownValues.get("A4").getColumnIndex());
        Assertions.assertEquals(4, knownValues.get("A4").getRowIndex());
        Assertions.assertEquals(new BigDecimal(3), knownValues.get("B5").getDerivedValue());
        Assertions.assertEquals("3.0", knownValues.get("B5").getDerivedValueTextRepresentation());
        Assertions.assertEquals("#(sum A4 B4)", knownValues.get("B5").getFunctionText());
        Assertions.assertEquals(1, knownValues.get("B5").getColumnIndex());
        Assertions.assertEquals(5, knownValues.get("B5").getRowIndex());
        Assertions.assertEquals(new BigDecimal(3), knownValues.get("D3").getDerivedValue());
        Assertions.assertEquals("3.0", knownValues.get("D3").getDerivedValueTextRepresentation());
        Assertions.assertEquals("#(prod B5 A4)", knownValues.get("D3").getFunctionText());
        Assertions.assertEquals(3, knownValues.get("D3").getColumnIndex());
        Assertions.assertEquals(3, knownValues.get("D3").getRowIndex());
    }
}
