package com.elementalrift.model;

import javafx.beans.property.*;

/**
 * Data model for game history records presented in JavaFX TableView.
 * Uses JavaFX Property bindings for clean table integration.
 * [OOP Requirement: Classes & Objects]
 * [OOP Requirement: Encapsulation]
 */
public class GameRecord {
    private final StringProperty playerName;
    private final IntegerProperty score;
    private final IntegerProperty waveReached;
    private final IntegerProperty enemiesKilled;
    private final DoubleProperty elementalAccuracy;
    private final StringProperty timestamp;

    public GameRecord(String playerName, int score, int waveReached, int enemiesKilled, double elementalAccuracy, String timestamp) {
        this.playerName = new SimpleStringProperty(playerName);
        this.score = new SimpleIntegerProperty(score);
        this.waveReached = new SimpleIntegerProperty(waveReached);
        this.enemiesKilled = new SimpleIntegerProperty(enemiesKilled);
        this.elementalAccuracy = new SimpleDoubleProperty(elementalAccuracy);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public String getPlayerName() { return playerName.get(); }
    public StringProperty playerNameProperty() { return playerName; }

    public int getScore() { return score.get(); }
    public IntegerProperty scoreProperty() { return score; }

    public int getWaveReached() { return waveReached.get(); }
    public IntegerProperty waveReachedProperty() { return waveReached; }

    public int getEnemiesKilled() { return enemiesKilled.get(); }
    public IntegerProperty enemiesKilledProperty() { return enemiesKilled; }

    public double getElementalAccuracy() { return elementalAccuracy.get(); }
    public DoubleProperty elementalAccuracyProperty() { return elementalAccuracy; }

    public String getTimestamp() { return timestamp.get(); }
    public StringProperty timestampProperty() { return timestamp; }

    /**
     * Converts to CSV format for persistence.
     */
    public String toCsvLine() {
        return String.format("%s,%d,%d,%d,%.2f,%s",
                getPlayerName(), getScore(), getWaveReached(), getEnemiesKilled(), getElementalAccuracy(), getTimestamp());
    }

    /**
     * Parses from a CSV line.
     */
    public static GameRecord fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid CSV record line: " + line);
        }
        return new GameRecord(
                parts[0].trim(),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim()),
                Integer.parseInt(parts[3].trim()),
                Double.parseDouble(parts[4].trim()),
                parts[5].trim()
        );
    }
}
