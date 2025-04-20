package com.stephanstrydom.spreadsheet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import static com.stephanstrydom.spreadsheet.Spreadsheet.*;

public abstract class CsvReader {

    boolean stopReading = false;
    BufferedReader reader;

    public void read(Reader csvReader) throws IOException {
        try (BufferedReader reader = new BufferedReader(csvReader)) {
            this.reader = reader;
            while (reader.ready()) {
                int character = reader.read();
                switch (character) {
                    case -1 -> {
                        stopReading = true; //some readers terminate this way
                    }
                    case DELIMITER -> handleDelimiter();
                    case '\n'-> handleNewline();
                    case '\r'-> {}
                    case '"' -> handleQuote();
                    case '#' -> handleHash();
                    default -> handleCellCharacter(character);
                }
                postRead();
                if(stopReading) break;
            }
        }
    }

    public BufferedReader getBufferedReader() {
        return reader;
    }

    protected void stopReading() {
        stopReading = true;
    }

    protected abstract void handleDelimiter() throws IOException ;

    protected abstract void handleNewline() throws IOException ;

    protected abstract void handleQuote() throws IOException ;

    protected abstract void handleHash() throws IOException ;

    protected abstract void handleCellCharacter(int character) throws IOException ;

    protected abstract void postRead() throws IOException ;
}
