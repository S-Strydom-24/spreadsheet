package com.stephanstrydom.spreadsheet.util;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.stephanstrydom.spreadsheet.Spreadsheet.MAX_FUNCTION_SIZE;

public class SpreadsheetUtils {
    private static NumberFormat numberFormat;

    public static NumberFormat getNumberFormat() {
        if(numberFormat == null) {
            numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
            numberFormat.setMinimumFractionDigits(1);
        }
        return numberFormat;
    }

    //this is bad, I would normally use apache commons for this
    public static Optional<BigDecimal> toBigDecimal(String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException nfe) {
        }
        return Optional.empty();
    }

    public static String convertToCoordinate(long row, int column) {
        StringBuilder output = new StringBuilder();
        int temp = column;
        while (temp > 0) {
            temp--;
            output.insert(0, (char) ('A' + (temp % 26)));
            temp /= 26;
        }
        return output.append(row).toString();
    }

    public static String readUpTo(Reader reader, List<Character> endCharacters, boolean reset) throws IOException {
        if(reset)reader.mark(MAX_FUNCTION_SIZE);
        int characterCount = 0;
        StringBuilder output = new StringBuilder();
        try {
            while (reader.ready() && characterCount < MAX_FUNCTION_SIZE) {
                if(!reset)reader.mark(MAX_FUNCTION_SIZE);
                char nextCharacter = (char)reader.read();
                output.append(nextCharacter);
                if (endCharacters.contains(nextCharacter)) {
                    return output.deleteCharAt(output.length()-1).toString();
                }
                characterCount++;
            }
        } finally {
            if(reader.ready())reader.reset();
        }
        return output.toString();
    }

    public static boolean isEscapedQuote(Reader reader) throws IOException {
        StringBuilder threeCharacters = new StringBuilder();
        threeCharacters.append('"');
        reader.mark(3);
        while(reader.ready() && threeCharacters.length() < 3) threeCharacters.append((char)reader.read());
        boolean isEscapedQuote = threeCharacters.toString().equals("\"\"\"");
        if(!isEscapedQuote) reader.reset();
        return isEscapedQuote;
    }
}
