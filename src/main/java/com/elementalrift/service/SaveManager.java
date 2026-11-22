package com.elementalrift.service;

import com.elementalrift.exceptions.InvalidSaveFileException;
import com.elementalrift.model.GameRecord;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing high scores and game history persistence.
 * [OOP Requirement: Exception Handling - throws custom InvalidSaveFileException on corrupted data]
 */
public class SaveManager {
    private static final String DEFAULT_FILE_NAME = "elemental_rift_scores.csv";
    private final Path saveFilePath;

    public SaveManager() {
        this(Paths.get(DEFAULT_FILE_NAME));
    }

    public SaveManager(Path saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    /**
     * Saves a list of game records to the CSV file.
     * @param records List of records to write.
     * @throws IOException If writing fails.
     */
    public synchronized void saveRecords(List<GameRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(saveFilePath)) {
            writer.write("# PlayerName,Score,WaveReached,EnemiesKilled,ElementalAccuracy,Timestamp\n");
            for (GameRecord record : records) {
                writer.write(record.toCsvLine());
                writer.newLine();
            }
        }
    }

    /**
     * Appends a single record to the save file.
     */
    public synchronized void appendRecord(GameRecord record) throws IOException {
        boolean fileExists = Files.exists(saveFilePath);
        try (BufferedWriter writer = Files.newBufferedWriter(saveFilePath, 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!fileExists) {
                writer.write("# PlayerName,Score,WaveReached,EnemiesKilled,ElementalAccuracy,Timestamp\n");
            }
            writer.write(record.toCsvLine());
            writer.newLine();
        }
    }

    /**
     * Loads and validates all records from the save file.
     * [OOP Requirement: Custom Exception Handling - InvalidSaveFileException]
     * @return List of parsed records.
     * @throws InvalidSaveFileException If any line is corrupted, malformed, or has invalid data types.
     */
    public synchronized List<GameRecord> loadRecords() throws InvalidSaveFileException {
        List<GameRecord> records = new ArrayList<>();
        if (!Files.exists(saveFilePath)) {
            return records;
        }

        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(saveFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip comments and empty lines
                }

                try {
                    GameRecord record = GameRecord.fromCsvLine(line);
                    records.add(record);
                } catch (Exception e) {
                    throw new InvalidSaveFileException(
                            "Malformed CSV data: " + e.getMessage(), 
                            saveFilePath.toString(), lineNumber);
                }
            }
        } catch (IOException e) {
            throw new InvalidSaveFileException("Failed to read file: " + e.getMessage(), e);
        }

        return records;
    }
}
