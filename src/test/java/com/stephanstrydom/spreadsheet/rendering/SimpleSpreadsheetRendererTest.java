package com.stephanstrydom.spreadsheet.rendering;

import com.stephanstrydom.spreadsheet.common.TestComponents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SimpleSpreadsheetRendererTest {

    private static final String EXPECTED_OUTPUT = """
one                    |two             |three                                        |four|             |||
one                    |two             |three                                        |    |             |||
one                    |                |                                             | 3.0|             |||
                    1.0|             2.0|                                          3.0| 4.0|#(prod B5 A9)|||
-----------------------|             3.0|                                         34.0|    |             |||
                       |hello           |byeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee|    |             |||
escaped commas,,,,,,,,,| escaped quote "| blah                                        |    |             |||""";

    @Test
    void testRendering() throws IOException {
        String output = TestComponents.getRendering();
        Assertions.assertEquals(EXPECTED_OUTPUT.replace("\n","\r\n"), output);
    }
}
