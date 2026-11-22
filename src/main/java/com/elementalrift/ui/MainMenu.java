package com.elementalrift.ui;

import com.elementalrift.MainApp;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Main Menu Screen: Entry portal into the Elemental Rift arena.
 * [OOP Requirement: JavaFX GUI - Screen 1 of 4]
 * [OOP Requirement: JavaFX Event Handling - button click events dispatching screen transitions]
 */
public class MainMenu extends VBox {

    private final MainApp app;

    public MainMenu(MainApp app) {
        this.app = app;
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setSpacing(18);
        getStyleClass().add("root");
        setStyle("-fx-background-color: radial-gradient(center 50% 40%, radius 70%, #151b2e, #07090e);");

        Label title = new Label("ELEMENTAL RIFT");
        title.getStyleClass().add("title-banner");

        Label subtitle = new Label("Real-Time Elemental Mage Arena Defense");
        subtitle.getStyleClass().add("subtitle-banner");

        Label matchupHint = new Label("Elemental Counters: Water > Fire > Air > Earth > Water (2x Damage!)");
        matchupHint.setStyle("-fx-text-fill: #ffd166; -fx-font-size: 13px; -fx-padding: 0 0 10 0;");

        // Action Buttons
        Button startButton = new Button("Enter the Rift (Start Game)");
        startButton.getStyleClass().addAll("button", "primary-button");
        startButton.setPrefWidth(280);
        startButton.setPrefHeight(44);
        // Event Handling: Handler delegates to app/game engine
        startButton.setOnAction(e -> app.showGameScreen());

        Button shopButton = new Button("Arcane Emporium (Shop)");
        shopButton.getStyleClass().add("button");
        shopButton.setPrefWidth(280);
        shopButton.setPrefHeight(40);
        shopButton.setOnAction(e -> app.showShopScreen());

        Button statsButton = new Button("Grimoire of Trials (Stats & Records)");
        statsButton.getStyleClass().add("button");
        statsButton.setPrefWidth(280);
        statsButton.setPrefHeight(40);
        statsButton.setOnAction(e -> app.showStatsScreen());

        Button guideButton = new Button("Grimoire of Controls & Lore");
        guideButton.getStyleClass().add("button");
        guideButton.setPrefWidth(280);
        guideButton.setPrefHeight(40);
        guideButton.setOnAction(e -> showInstructionsAlert());

        Button quitButton = new Button("Abandon Rift (Exit)");
        quitButton.getStyleClass().addAll("button", "danger-button");
        quitButton.setPrefWidth(280);
        quitButton.setPrefHeight(38);
        quitButton.setOnAction(e -> app.getPrimaryStage().close());

        getChildren().addAll(title, subtitle, matchupHint, startButton, shopButton, statsButton, guideButton, quitButton);
    }

    private void showInstructionsAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grimoire of Elemental Rift");
        alert.setHeaderText("Arena Controls & Combat Doctrine");
        alert.setContentText(
                """
                CONTROLS:
                 - Aim: Move mouse cursor across arena.
                 - Cast Spell: Left-Click or on-screen spell buttons.
                 - Switch Spells: Keys 1 to 4 or click hotbar.
                   [1] Fire Spell (Inferno Blast) - High burst, counters Air.
                   [2] Water Spell (Tidal Surge) - 3-spray waves, counters Fire.
                   [3] Air Spell (Gale Lance) - Fast piercing vortex, counters Earth.
                   [4] Earth Spell (Boulder Quake) - Heavy seismic boulder, counters Water.
                 - Pause: Press 'P'.
                
                TACTICAL COUNTERS:
                 - Correct Counter = 2.0x Damage!
                 - Weak Matchup = 0.5x Damage.
                 - Neutral Matchup = 1.0x Damage.
                
                OBJECTIVE:
                 Defend the Nexus Crystal at all costs! Survive waves and defeat the Titan Bosses!
                """
        );
        alert.showAndWait();
    }
}
