package com.elementalrift.ui;

import com.elementalrift.MainApp;
import com.elementalrift.interfaces.Notifiable;
import com.elementalrift.model.GameState;
import com.elementalrift.model.Player;
import com.elementalrift.model.Spell;
import com.elementalrift.service.GameEngine;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Combat Arena Screen: renders the real-time elemental battlefield,
 * HUD indicators, spell hotbar, and manages user input handling and game loop.
 *
 * [OOP Requirement: JavaFX GUI - Screen 2 of 4]
 * [OOP Requirement: JavaFX Event Handling - mouse clicks/aim, keys 1-4, P for pause, menu actions]
 * [OOP Requirement: Interface - implements Notifiable for in-game combat log alerts]
 */
public class GameScreen extends BorderPane implements Notifiable {

    private final MainApp app;
    private final GameEngine gameEngine;
    private final Canvas canvas;
    private final GraphicsContext gc;

    // HUD controls
    private Label waveLabel;
    private Label scoreLabel;
    private Label goldLabel;
    private ProgressBar crystalHpBar;
    private Label crystalHpText;
    private ProgressBar playerManaBar;
    private Label playerManaText;
    private Label combatLogLabel;

    // Spell hotbar buttons
    private final List<Button> spellButtons = new ArrayList<>();

    // Overlays
    private VBox pauseOverlay;
    private VBox gameOverOverlay;

    // AnimationTimer game loop
    private AnimationTimer gameLoop;
    private long lastNanoTime = 0;

    public GameScreen(MainApp app, GameEngine gameEngine) {
        this.app = app;
        this.gameEngine = gameEngine;
        this.canvas = new Canvas(gameEngine.getArenaWidth(), gameEngine.getArenaHeight());
        this.gc = canvas.getGraphicsContext2D();

        // Subscribe to engine notifications
        gameEngine.getNotificationService().addListener(this);

        initUI();
        initEventHandlers();
        initGameLoop();
    }

    private void initUI() {
        setTop(createTopSection());

        // Center Arena StackPane (Canvas + Overlays)
        StackPane centerStack = new StackPane();
        centerStack.getChildren().add(canvas);

        // Create Pause and Game Over Overlays
        pauseOverlay = createPauseOverlay();
        gameOverOverlay = createGameOverOverlay();
        centerStack.getChildren().addAll(pauseOverlay, gameOverOverlay);

        setCenter(centerStack);
        setBottom(createSpellHotbar());
    }

    private VBox createTopSection() {
        VBox topContainer = new VBox();

        // Top MenuBar
        MenuBar menuBar = new MenuBar();
        Menu gameMenu = new Menu("Game");
        MenuItem pauseItem = new MenuItem("Pause / Resume (P)");
        pauseItem.setOnAction(e -> togglePause());

        MenuItem restartItem = new MenuItem("Restart Arena");
        restartItem.setOnAction(e -> restartGame());

        MenuItem menuItem = new MenuItem("Return to Main Menu");
        menuItem.setOnAction(e -> app.showMainMenu());

        MenuItem quitItem = new MenuItem("Exit Game");
        quitItem.setOnAction(e -> app.getPrimaryStage().close());

        gameMenu.getItems().addAll(pauseItem, restartItem, new SeparatorMenuItem(), menuItem, quitItem);

        Menu viewMenu = new Menu("Navigation");
        MenuItem shopItem = new MenuItem("Arcane Emporium (Shop)");
        shopItem.setOnAction(e -> app.showShopScreen());

        MenuItem statsItem = new MenuItem("Grimoire of Stats & Records");
        statsItem.setOnAction(e -> app.showStatsScreen());

        viewMenu.getItems().addAll(shopItem, statsItem);

        Menu helpMenu = new Menu("Help");
        MenuItem countersItem = new MenuItem("Elemental Counters");
        countersItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Elemental Counters");
            alert.setHeaderText("Elemental Advantage Cycle");
            alert.setContentText("Water douses Fire (2x)\nFire consumes Air (2x)\nAir erodes Earth (2x)\nEarth absorbs Water (2x)");
            alert.showAndWait();
        });
        helpMenu.getItems().add(countersItem);

        menuBar.getMenus().addAll(gameMenu, viewMenu, helpMenu);

        // Top HUD Status Bar
        HBox hud = new HBox(16);
        hud.getStyleClass().add("hud-panel");
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(6, 16, 6, 16));

        waveLabel = new Label("Wave: 1");
        waveLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ffd166;");

        goldLabel = new Label("Gold: 50g");
        goldLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f4a261;");

        // Crystal HP Display
        VBox crystalBox = new VBox(2);
        Label crystalTitle = new Label("Nexus Crystal HP");
        crystalTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #8ba2c4;");
        crystalHpBar = new ProgressBar(1.0);
        crystalHpBar.getStyleClass().addAll("progress-bar", "hp-bar");
        crystalHpBar.setPrefWidth(160);
        crystalHpBar.setPrefHeight(12);
        crystalHpText = new Label("1000 / 1000");
        crystalHpText.setStyle("-fx-font-size: 10px; -fx-text-fill: #ffffff;");
        crystalBox.getChildren().addAll(crystalTitle, crystalHpBar, crystalHpText);

        // Player Mana Display
        VBox manaBox = new VBox(2);
        Label manaTitle = new Label("Mage Mana");
        manaTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #8ba2c4;");
        playerManaBar = new ProgressBar(1.0);
        playerManaBar.getStyleClass().addAll("progress-bar", "mana-bar");
        playerManaBar.setPrefWidth(140);
        playerManaBar.setPrefHeight(12);
        playerManaText = new Label("150 / 150");
        playerManaText.setStyle("-fx-font-size: 10px; -fx-text-fill: #ffffff;");
        manaBox.getChildren().addAll(manaTitle, playerManaBar, playerManaText);

        // Quick Navigation Buttons
        Button quickShop = new Button("Shop");
        quickShop.getStyleClass().add("button");
        quickShop.setOnAction(e -> app.showShopScreen());

        Button quickStats = new Button("Stats");
        quickStats.getStyleClass().add("button");
        quickStats.setOnAction(e -> app.showStatsScreen());

        hud.getChildren().addAll(waveLabel, scoreLabel, goldLabel, crystalBox, manaBox, quickShop, quickStats);

        // In-game combat notification bar
        combatLogLabel = new Label("Defend the Nexus Crystal against elemental invaders!");
        combatLogLabel.setStyle("-fx-text-fill: #90e0ef; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 3 0 3 14;");

        topContainer.getChildren().addAll(menuBar, hud, combatLogLabel);
        return topContainer;
    }

    private HBox createSpellHotbar() {
        HBox hotbar = new HBox(12);
        hotbar.setAlignment(Pos.CENTER);
        hotbar.setPadding(new Insets(8, 12, 10, 12));
        hotbar.setStyle("-fx-background-color: #0b0d14; -fx-border-color: #1f273d; -fx-border-width: 1 0 0 0;");

        List<Spell> spells = gameEngine.getPlayer().getSpells();
        String[] hotkeys = {"[1]", "[2]", "[3]", "[4]"};
        String[] styleClasses = {"spell-fire", "spell-water", "spell-air", "spell-earth"};

        for (int i = 0; i < spells.size(); i++) {
            final int index = i;
            Spell s = spells.get(i);

            Button btn = new Button(String.format("%s %s\n(%.0f Mana)", hotkeys[i], s.getName(), s.getManaCost()));
            btn.getStyleClass().addAll("button", "spell-button", styleClasses[i % styleClasses.length]);

            // Event Handling: Button click selects spell
            btn.setOnAction(e -> {
                gameEngine.getPlayer().selectSpell(index);
                updateHotbarSelection();
            });

            spellButtons.add(btn);
            hotbar.getChildren().add(btn);
        }

        updateHotbarSelection();
        return hotbar;
    }

    private VBox createPauseOverlay() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(5, 7, 12, 0.78);");
        box.setVisible(false);

        Label pauseTitle = new Label("RITUAL SUSPENDED (PAUSED)");
        pauseTitle.getStyleClass().add("title-banner");
        pauseTitle.setStyle("-fx-font-size: 26px; -fx-text-fill: #ffd166;");

        Button resumeBtn = new Button("Resume Battle");
        resumeBtn.getStyleClass().addAll("button", "primary-button");
        resumeBtn.setPrefWidth(200);
        resumeBtn.setOnAction(e -> togglePause());

        Button restartBtn = new Button("Restart Arena");
        restartBtn.getStyleClass().add("button");
        restartBtn.setPrefWidth(200);
        restartBtn.setOnAction(e -> restartGame());

        Button menuBtn = new Button("Main Menu");
        menuBtn.getStyleClass().add("button");
        menuBtn.setPrefWidth(200);
        menuBtn.setOnAction(e -> app.showMainMenu());

        box.getChildren().addAll(pauseTitle, resumeBtn, restartBtn, menuBtn);
        return box;
    }

    private VBox createGameOverOverlay() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(15, 3, 5, 0.88);");
        box.setVisible(false);

        Label overTitle = new Label("THE CRYSTAL HAS FALLEN");
        overTitle.getStyleClass().add("title-banner");
        overTitle.setStyle("-fx-font-size: 28px; -fx-text-fill: #ff3366;");

        Label overSummary = new Label("The elemental rifts breached the sacred defenses.");
        overSummary.setStyle("-fx-text-fill: #dce5f2; -fx-font-size: 14px;");

        Button tryAgainBtn = new Button("Try Again (Restart)");
        tryAgainBtn.getStyleClass().addAll("button", "primary-button");
        tryAgainBtn.setPrefWidth(220);
        tryAgainBtn.setOnAction(e -> restartGame());

        Button viewStatsBtn = new Button("View Battle Analytics");
        viewStatsBtn.getStyleClass().add("button");
        viewStatsBtn.setPrefWidth(220);
        viewStatsBtn.setOnAction(e -> app.showStatsScreen());

        Button menuBtn = new Button("Main Menu");
        menuBtn.getStyleClass().add("button");
        menuBtn.setPrefWidth(220);
        menuBtn.setOnAction(e -> app.showMainMenu());

        box.getChildren().addAll(overTitle, overSummary, tryAgainBtn, viewStatsBtn, menuBtn);
        return box;
    }

    /**
     * JavaFX Event Handling: sets up mouse movement, clicks, and keyboard events.
     * [OOP Requirement: JavaFX Event Handling - No game logic in handlers, delegates to GameEngine]
     */
    private void initEventHandlers() {
        // Track mouse aim
        canvas.setOnMouseMoved(e -> {
            Player p = gameEngine.getPlayer();
            double angle = Math.atan2(e.getY() - p.getCenterY(), e.getX() - p.getCenterX());
            p.setAimAngle(angle);
        });

        // Cast spell on mouse click
        canvas.setOnMouseClicked(e -> {
            gameEngine.castSelectedSpell(e.getX(), e.getY());
        });

        // Continuous casting / aiming when dragging
        canvas.setOnMouseDragged(e -> {
            Player p = gameEngine.getPlayer();
            double angle = Math.atan2(e.getY() - p.getCenterY(), e.getX() - p.getCenterX());
            p.setAimAngle(angle);
            gameEngine.castSelectedSpell(e.getX(), e.getY());
        });

        // Keyboard navigation (1-4, P)
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DIGIT1) {
                gameEngine.getPlayer().selectSpell(0);
                updateHotbarSelection();
            } else if (e.getCode() == KeyCode.DIGIT2) {
                gameEngine.getPlayer().selectSpell(1);
                updateHotbarSelection();
            } else if (e.getCode() == KeyCode.DIGIT3) {
                gameEngine.getPlayer().selectSpell(2);
                updateHotbarSelection();
            } else if (e.getCode() == KeyCode.DIGIT4) {
                gameEngine.getPlayer().selectSpell(3);
                updateHotbarSelection();
            } else if (e.getCode() == KeyCode.P) {
                togglePause();
            }
        });

        setFocusTraversable(true);
    }

    private void updateHotbarSelection() {
        int selected = gameEngine.getPlayer().getSelectedSpellIndex();
        for (int i = 0; i < spellButtons.size(); i++) {
            Button btn = spellButtons.get(i);
            btn.getStyleClass().remove("spell-active");
            if (i == selected) {
                btn.getStyleClass().add("spell-active");
            }
        }
    }

    private void togglePause() {
        if (gameEngine.getGameState() == GameState.PLAYING) {
            gameEngine.setGameState(GameState.PAUSED);
            pauseOverlay.setVisible(true);
        } else if (gameEngine.getGameState() == GameState.PAUSED) {
            gameEngine.setGameState(GameState.PLAYING);
            pauseOverlay.setVisible(false);
            requestFocus();
        }
    }

    public void restartGame() {
        pauseOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);
        gameEngine.restartGame();
        requestFocus();
    }

    /**
     * Initializes the 60 FPS JavaFX AnimationTimer game loop.
     */
    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanoTime == 0) {
                    lastNanoTime = now;
                    return;
                }

                double delta = (now - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = now;

                // Clamp delta to avoid huge physics leaps after lag spikes
                if (delta > 0.1) delta = 0.1;

                // Update game simulation
                gameEngine.update(delta);

                // Render game arena
                gameEngine.render(gc);

                // Update HUD indicators
                updateHUD();
            }
        };
    }

    private void updateHUD() {
        Player p = gameEngine.getPlayer();
        var crystal = gameEngine.getCrystal();
        var wm = gameEngine.getWaveManager();

        waveLabel.setText("Wave: " + wm.getCurrentWave() + (wm.isCurrentWaveBossWave() ? " [BOSS!]" : ""));
        scoreLabel.setText("Score: " + p.getScore());
        goldLabel.setText("Gold: " + p.getGold() + "g");

        // Crystal HP
        double crystalRatio = crystal.getHealthRatio();
        crystalHpBar.setProgress(crystalRatio);
        crystalHpText.setText(String.format("%.0f / %.0f", crystal.getHealth(), crystal.getMaxHealth()));

        // Player Mana
        double manaRatio = p.getMana() / p.getMaxMana();
        playerManaBar.setProgress(manaRatio);
        playerManaText.setText(String.format("%.0f / %.0f", p.getMana(), p.getMaxMana()));

        // Show Game Over overlay if triggered
        if (gameEngine.getGameState() == GameState.GAME_OVER) {
            gameOverOverlay.setVisible(true);
        }
    }

    public void startGame() {
        if (gameEngine.getGameState() == GameState.MENU || gameEngine.getGameState() == GameState.GAME_OVER) {
            restartGame();
        }
        gameLoop.start();
        requestFocus();
    }

    public void stopGame() {
        gameLoop.stop();
    }

    @Override
    public void onNotification(String message, String type) {
        combatLogLabel.setText(message);
        if ("DANGER".equals(type)) {
            combatLogLabel.setStyle("-fx-text-fill: #ff4d6d; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if ("SUCCESS".equals(type)) {
            combatLogLabel.setStyle("-fx-text-fill: #48cae4; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if ("WARNING".equals(type)) {
            combatLogLabel.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            combatLogLabel.setStyle("-fx-text-fill: #90e0ef; -fx-font-style: italic; -fx-font-size: 11px;");
        }
    }
}
