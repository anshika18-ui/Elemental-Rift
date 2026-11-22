package com.elementalrift.service;

import com.elementalrift.model.Element;
import javafx.application.Platform;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Background statistics and analytical report generator.
 * [OOP Requirement: Multithreading - Thread 2 for background report calculation]
 * Runs independently in a background worker thread with progress updates via Platform.runLater().
 * Prevents GUI freezing when processing large datasets or generating complex combat summaries.
 */
public class ReportGenerator implements Runnable {

    public static class AnalyticsReport {
        private final Map<Element, Double> damageDealtByElement;
        private final Map<String, Integer> killsByEnemyType;
        private final int totalKills;
        private final double totalDamage;
        private final double counterAccuracy; // Percentage of 2.0x counter hits
        private final String performanceGrade;
        private final String summaryText;

        public AnalyticsReport(Map<Element, Double> damageByElement, Map<String, Integer> killsByType,
                               int totalKills, double totalDamage, double counterAccuracy,
                               String grade, String summary) {
            this.damageDealtByElement = damageByElement;
            this.killsByEnemyType = killsByType;
            this.totalKills = totalKills;
            this.totalDamage = totalDamage;
            this.counterAccuracy = counterAccuracy;
            this.performanceGrade = grade;
            this.summaryText = summary;
        }

        public Map<Element, Double> getDamageDealtByElement() { return damageDealtByElement; }
        public Map<String, Integer> getKillsByEnemyType() { return killsByEnemyType; }
        public int getTotalKills() { return totalKills; }
        public double getTotalDamage() { return totalDamage; }
        public double getCounterAccuracy() { return counterAccuracy; }
        public String getPerformanceGrade() { return performanceGrade; }
        public String getSummaryText() { return summaryText; }
    }

    private final Map<Element, Double> rawDamage;
    private final Map<String, Integer> rawKills;
    private final int successfulCounters;
    private final int totalHits;
    private final Consumer<Double> progressCallback;
    private final Consumer<AnalyticsReport> completionCallback;

    public ReportGenerator(Map<Element, Double> rawDamage, Map<String, Integer> rawKills,
                           int successfulCounters, int totalHits,
                           Consumer<Double> progressCallback,
                           Consumer<AnalyticsReport> completionCallback) {
        this.rawDamage = new ConcurrentHashMap<>(rawDamage);
        this.rawKills = new ConcurrentHashMap<>(rawKills);
        this.successfulCounters = successfulCounters;
        this.totalHits = totalHits;
        this.progressCallback = progressCallback;
        this.completionCallback = completionCallback;
    }

    @Override
    public void run() {
        try {
            // Stage 1: Aggregate damage statistics (simulating heavy analytics computation)
            updateProgress(0.20);
            Thread.sleep(120);

            double totalDamage = 0.0;
            for (double d : rawDamage.values()) {
                totalDamage += d;
            }

            // Stage 2: Aggregate kill counts by enemy class
            updateProgress(0.50);
            Thread.sleep(150);

            int totalKills = 0;
            for (int k : rawKills.values()) {
                totalKills += k;
            }

            // Stage 3: Calculate elemental counter accuracy
            updateProgress(0.80);
            Thread.sleep(100);

            double accuracy = (totalHits > 0) ? ((double) successfulCounters / totalHits) * 100.0 : 0.0;

            // Stage 4: Assign performance grade
            String grade;
            if (accuracy >= 80.0 && totalKills >= 25) {
                grade = "S - Archmage of the Elements";
            } else if (accuracy >= 65.0) {
                grade = "A - Master Elementalist";
            } else if (accuracy >= 45.0) {
                grade = "B - Adept Mage";
            } else {
                grade = "C - Novice Apprentice";
            }

            String summary = String.format(
                    "Total Elemental Damage: %.1f HP\n" +
                    "Foes Vanquished: %d\n" +
                    "Elemental Counter Precision: %.1f%%\n" +
                    "Evaluation Grade: %s",
                    totalDamage, totalKills, accuracy, grade
            );

            AnalyticsReport report = new AnalyticsReport(
                    rawDamage, rawKills, totalKills, totalDamage, accuracy, grade, summary
            );

            // Final stage: complete progress and deliver report on JavaFX thread
            updateProgress(1.0);
            if (completionCallback != null) {
                try {
                    Platform.runLater(() -> completionCallback.accept(report));
                } catch (IllegalStateException e) {
                    // Fallback for headless testing
                    completionCallback.accept(report);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateProgress(double progress) {
        if (progressCallback != null) {
            try {
                Platform.runLater(() -> progressCallback.accept(progress));
            } catch (IllegalStateException e) {
                // Fallback for headless testing
                progressCallback.accept(progress);
            }
        }
    }
}
