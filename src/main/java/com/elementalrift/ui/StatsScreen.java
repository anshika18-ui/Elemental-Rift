package com.elementalrift.ui;

import com.elementalrift.MainApp;
import com.elementalrift.exceptions.InvalidSaveFileException;
import com.elementalrift.model.Element;
import com.elementalrift.model.GameRecord;
import com.elementalrift.service.GameEngine;
import com.elementalrift.service.ReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Grimoire of Trials (Stats and Analytics Screen).
 * Displays high score records in a TableView, combat log in a ListView,
 * filter options with ComboBox, and triggers a multithreaded analytical report generator (Thread 2)
 * with a live ProgressBar.
 *
 * [OOP Requirement: JavaFX GUI - Screen 4 of 4]
 * [OOP Requirement: Controls - TableView, ListView, ComboBox, ProgressBar, Alert, Button]
 * [OOP Requirement: JavaFX Event Handling - table selection events and button clicks]
 * [OOP Requirement: Multithreading - Thread 2 background execution with Platform.runLater progress update]
 */
public class StatsScreen extends BorderPane {

    private final MainApp app;
    private final GameEngine gameEngine;

    // Controls
    private TableView<GameRecord> tableView;
    private ObservableList<GameRecord> masterRecordList;
    private ComboBox<String> filterComboBox;
    private ListView<String> combatLogListView;
    private ProgressBar analyticsProgressBar;
    private Label progressLabel;
    private TextArea analyticsResultArea;
    private Label selectedRecordDetailsLabel;

    public StatsScreen(MainApp app, GameEngine gameEngine) {
        this.app = app;
        this.gameEngine = gameEngine;
        initUI();
        loadSavedScores();
    }

    private void initUI() {
        setPadding(new Insets(16));
        getStyleClass().add("root");

        // Top Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 12, 16, 12));

        Label title = new Label("GRIMOIRE OF COMBAT STATS");
        title.getStyleClass().add("title-banner");
        title.setStyle("-fx-font-size: 24px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveCurrentBtn = new Button("Record Current Trial");
        saveCurrentBtn.getStyleClass().addAll("button", "primary-button");
        saveCurrentBtn.setOnAction(e -> saveCurrentRunRecord());

        Button backBtn = new Button("Back to Arena");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> app.showGameScreen());

        Button menuBtn = new Button("Main Menu");
        menuBtn.getStyleClass().add("button");
        menuBtn.setOnAction(e -> app.showMainMenu());

        header.getChildren().addAll(title, spacer, saveCurrentBtn, backBtn, menuBtn);
        setTop(header);

        // Center split pane: Left is TableView + Controls, Right is Analytics + Combat Log
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(createLeftLeaderboardPane(), createRightAnalyticsPane());
        splitPane.setDividerPositions(0.55);

        setCenter(splitPane);
    }

    private VBox createLeftLeaderboardPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(10));

        // Filter bar with ComboBox
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter Records:");
        filterLabel.setStyle("-fx-text-fill: #8ba2c4; -fx-font-weight: bold;");

        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Trials", "High Scores (> 2,000)", "High Waves (Wave >= 3)");
        filterComboBox.setValue("All Trials");
        // JavaFX Event Handling: ComboBox selection changes table view filter
        filterComboBox.setOnAction(e -> applyFilter(filterComboBox.getValue()));

        filterBar.getChildren().addAll(filterLabel, filterComboBox);

        // TableView
        tableView = new TableView<>();
        masterRecordList = FXCollections.observableArrayList();

        TableColumn<GameRecord, String> playerCol = new TableColumn<>("Mage Name");
        playerCol.setCellValueFactory(cellData -> cellData.getValue().playerNameProperty());
        playerCol.setPrefWidth(120);

        TableColumn<GameRecord, Number> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(cellData -> cellData.getValue().scoreProperty());
        scoreCol.setPrefWidth(85);

        TableColumn<GameRecord, Number> waveCol = new TableColumn<>("Wave");
        waveCol.setCellValueFactory(cellData -> cellData.getValue().waveReachedProperty());
        waveCol.setPrefWidth(65);

        TableColumn<GameRecord, Number> killsCol = new TableColumn<>("Kills");
        killsCol.setCellValueFactory(cellData -> cellData.getValue().enemiesKilledProperty());
        killsCol.setPrefWidth(65);

        TableColumn<GameRecord, Number> accCol = new TableColumn<>("Counter %");
        accCol.setCellValueFactory(cellData -> cellData.getValue().elementalAccuracyProperty());
        accCol.setPrefWidth(85);

        TableColumn<GameRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
        dateCol.setPrefWidth(120);

        tableView.getColumns().addAll(playerCol, scoreCol, waveCol, killsCol, accCol, dateCol);
        tableView.setItems(masterRecordList);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Event Handling: Table selection event
        selectedRecordDetailsLabel = new Label("Select a trial row above to inspect archive details.");
        selectedRecordDetailsLabel.setStyle("-fx-text-fill: #ffd166; -fx-font-size: 11px;");

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRecordDetailsLabel.setText(String.format(
                        "Archive Details: Mage '%s' survived to Wave %d with %d kills and %.1f%% elemental advantage counter accuracy.",
                        newVal.getPlayerName(), newVal.getWaveReached(), newVal.getEnemiesKilled(), newVal.getElementalAccuracy()
                ));
            }
        });

        box.getChildren().addAll(filterBar, tableView, selectedRecordDetailsLabel);
        return box;
    }

    private VBox createRightAnalyticsPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(10));

        // Background Thread Report Generator Section
        Label reportTitle = new Label("Multithreaded Analytics Generator (Thread 2)");
        reportTitle.getStyleClass().add("section-header");
        reportTitle.setStyle("-fx-font-size: 15px;");

        Label reportDesc = new Label("Analyzes damage-per-element, counter precision, and assigns an Archmage grade.");
        reportDesc.setStyle("-fx-text-fill: #8ba2c4; -fx-font-size: 11px;");

        Button runAnalyticsBtn = new Button("Run Background Deep Analysis");
        runAnalyticsBtn.getStyleClass().addAll("button", "primary-button");

        analyticsProgressBar = new ProgressBar(0.0);
        analyticsProgressBar.getStyleClass().addAll("progress-bar", "analytics-bar");
        analyticsProgressBar.setPrefWidth(320);

        progressLabel = new Label("Analysis Idle");
        progressLabel.setStyle("-fx-text-fill: #99aabf; -fx-font-size: 11px;");

        analyticsResultArea = new TextArea();
        analyticsResultArea.setEditable(false);
        analyticsResultArea.setPrefHeight(130);
        analyticsResultArea.setStyle("-fx-control-inner-background: #0d101a; -fx-font-family: monospace; -fx-font-size: 12px;");
        analyticsResultArea.setText("Press 'Run Background Deep Analysis' to invoke Thread 2...");

        // Event Handling: Launch background thread
        runAnalyticsBtn.setOnAction(e -> {
            runAnalyticsBtn.setDisable(true);
            progressLabel.setText("Worker Thread computing elemental breakdowns...");

            gameEngine.generateAnalyticsReport(
                    progress -> {
                        analyticsProgressBar.setProgress(progress);
                        progressLabel.setText(String.format("Analyzing Combat Streams: %.0f%%", progress * 100));
                    },
                    report -> {
                        runAnalyticsBtn.setDisable(false);
                        progressLabel.setText("Analysis Finished! Result ready.");

                        StringBuilder sb = new StringBuilder();
                        sb.append(report.getSummaryText()).append("\n\n");
                        sb.append("--- Damage Dealt per Element ---\n");
                        for (Element elem : Element.values()) {
                            if (elem != Element.NONE) {
                                double dmg = report.getDamageDealtByElement().getOrDefault(elem, 0.0);
                                sb.append(String.format(" * %-6s: %.1f HP\n", elem.getDisplayName(), dmg));
                            }
                        }
                        sb.append("\n--- Foes Defeated by Species ---\n");
                        report.getKillsByEnemyType().forEach((type, count) -> {
                            sb.append(String.format(" * %-10s: %d\n", type, count));
                        });

                        analyticsResultArea.setText(sb.toString());
                    }
            );
        });

        // Recent Combat Notifications History (ListView)
        Label logTitle = new Label("Live Combat Log History");
        logTitle.getStyleClass().add("section-header");
        logTitle.setStyle("-fx-font-size: 14px;");

        combatLogListView = new ListView<>();
        combatLogListView.getItems().addAll(gameEngine.getNotificationService().getMessageHistory());
        combatLogListView.setPrefHeight(150);

        box.getChildren().addAll(
                reportTitle, reportDesc, runAnalyticsBtn,
                analyticsProgressBar, progressLabel, analyticsResultArea,
                logTitle, combatLogListView
        );
        return box;
    }

    private void applyFilter(String filter) {
        if ("High Scores (> 2,000)".equals(filter)) {
            tableView.setItems(masterRecordList.filtered(r -> r.getScore() > 2000));
        } else if ("High Waves (Wave >= 3)".equals(filter)) {
            tableView.setItems(masterRecordList.filtered(r -> r.getWaveReached() >= 3));
        } else {
            tableView.setItems(masterRecordList);
        }
    }

    private void loadSavedScores() {
        try {
            List<GameRecord> saved = gameEngine.getSaveManager().loadRecords();
            masterRecordList.clear();
            masterRecordList.addAll(saved);
        } catch (InvalidSaveFileException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save File Error");
            alert.setHeaderText("Corrupted Save Archive Detected");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void saveCurrentRunRecord() {
        var p = gameEngine.getPlayer();
        var wm = gameEngine.getWaveManager();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        GameRecord record = new GameRecord(
                "Archmage",
                p.getScore(),
                wm.getCurrentWave(),
                p.getEnemiesDefeated(),
                80.0,
                timestamp
        );

        masterRecordList.add(record);
        try {
            gameEngine.getSaveManager().appendRecord(record);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Current trial successfully recorded to archive!");
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to persist record: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void refreshCombatLog() {
        combatLogListView.getItems().clear();
        combatLogListView.getItems().addAll(gameEngine.getNotificationService().getMessageHistory());
    }
}
