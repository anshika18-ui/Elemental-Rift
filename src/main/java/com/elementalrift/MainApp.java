package com.elementalrift;

import com.elementalrift.service.GameEngine;
import com.elementalrift.ui.GameScreen;
import com.elementalrift.ui.MainMenu;
import com.elementalrift.ui.ShopScreen;
import com.elementalrift.ui.StatsScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main JavaFX Application entry point for "Elemental Rift".
 * Manages scenes, window life cycle, CSS injection, and orchestrates transitions between screens:
 * Main Menu -> Game Screen -> Shop Screen -> Stats Screen.
 *
 * [OOP Requirement: JavaFX GUI - 4 distinct integrated screens]
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private GameEngine gameEngine;
    private Scene scene;

    private MainMenu mainMenu;
    private GameScreen gameScreen;
    private ShopScreen shopScreen;
    private StatsScreen statsScreen;

    public static final double WIDTH = 860;
    public static final double HEIGHT = 640;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Elemental Rift - Real-Time Elemental Arena Defense");

        // Instantiate core engine (heap objects, not static)
        this.gameEngine = new GameEngine(WIDTH, HEIGHT - 110);

        // Instantiate screens
        this.mainMenu = new MainMenu(this);
        this.gameScreen = new GameScreen(this, gameEngine);
        this.shopScreen = new ShopScreen(this, gameEngine);
        this.statsScreen = new StatsScreen(this, gameEngine);

        // Initialize master scene
        this.scene = new Scene(mainMenu, WIDTH, HEIGHT);

        // Load Dark Fantasy CSS Stylesheet
        try {
            String css = Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Warning: Could not load /style.css, continuing with default theme.");
        }

        // Clean background thread shutdown when window closes
        primaryStage.setOnCloseRequest(e -> {
            gameScreen.stopGame();
            gameEngine.shutdown();
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void showMainMenu() {
        gameScreen.stopGame();
        scene.setRoot(mainMenu);
    }

    public void showGameScreen() {
        scene.setRoot(gameScreen);
        gameScreen.startGame();
    }

    public void showShopScreen() {
        gameScreen.stopGame();
        shopScreen.refreshUI();
        scene.setRoot(shopScreen);
    }

    public void showStatsScreen() {
        gameScreen.stopGame();
        statsScreen.refreshCombatLog();
        scene.setRoot(statsScreen);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
