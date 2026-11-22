package com.elementalrift.ui;

import com.elementalrift.MainApp;
import com.elementalrift.model.Spell;
import com.elementalrift.service.GameEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Arcane Emporium (Shop / Upgrade Screen).
 * Allows the elemental mage to spend gold on upgrading spell tiers,
 * improving damage, lowering cooldowns, and fortifying the Nexus Crystal.
 *
 * [OOP Requirement: JavaFX GUI - Screen 3 of 4]
 * [OOP Requirement: Controls - TabPane, Button, Label, ProgressBar]
 * [OOP Requirement: Interface - uses Upgradable methods to enhance spells]
 */
public class ShopScreen extends BorderPane {

    private final MainApp app;
    private final GameEngine gameEngine;
    private Label goldHeaderLabel;
    private VBox spellCardsContainer;

    public ShopScreen(MainApp app, GameEngine gameEngine) {
        this.app = app;
        this.gameEngine = gameEngine;
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(16));
        getStyleClass().add("root");

        // Top Navigation Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 12, 16, 12));

        Label shopTitle = new Label("ARCANE EMPORIUM");
        shopTitle.getStyleClass().add("title-banner");
        shopTitle.setStyle("-fx-font-size: 26px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        goldHeaderLabel = new Label("Gold: " + gameEngine.getPlayer().getGold() + "g");
        goldHeaderLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffd166;");

        Button returnBtn = new Button("Back to Arena");
        returnBtn.getStyleClass().addAll("button", "primary-button");
        returnBtn.setOnAction(e -> app.showGameScreen());

        Button menuBtn = new Button("Main Menu");
        menuBtn.getStyleClass().add("button");
        menuBtn.setOnAction(e -> app.showMainMenu());

        header.getChildren().addAll(shopTitle, spacer, goldHeaderLabel, returnBtn, menuBtn);
        setTop(header);

        // Center TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab spellsTab = new Tab("Elemental Spells", createSpellsTabContent());
        Tab crystalTab = new Tab("Crystal Bastion", createCrystalTabContent());
        Tab mageTab = new Tab("Mage Mastery", createMageTabContent());

        tabPane.getTabs().addAll(spellsTab, crystalTab, mageTab);
        setCenter(tabPane);
    }

    private ScrollPane createSpellsTabContent() {
        spellCardsContainer = new VBox(14);
        spellCardsContainer.setPadding(new Insets(14));

        refreshSpellCards();

        ScrollPane scroll = new ScrollPane(spellCardsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    private void refreshSpellCards() {
        spellCardsContainer.getChildren().clear();
        List<Spell> spells = gameEngine.getPlayer().getSpells();

        for (int i = 0; i < spells.size(); i++) {
            final int index = i;
            Spell s = spells.get(i);

            HBox card = new HBox(16);
            card.getStyleClass().add("arcane-card");
            card.setAlignment(Pos.CENTER_LEFT);

            // Element icon badge
            VBox badge = new VBox(4);
            badge.setAlignment(Pos.CENTER);
            badge.setPrefWidth(90);
            Label elementLabel = new Label(s.getElement().getDisplayName().toUpperCase());
            elementLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + 
                    toHex(s.getElement().getPrimaryColor()) + ";");
            Label tierLabel = new Label("Tier " + s.getLevel());
            tierLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8ba2c4;");
            badge.getChildren().addAll(elementLabel, tierLabel);

            // Details
            VBox details = new VBox(4);
            HBox.setHgrow(details, Priority.ALWAYS);
            Label nameLabel = new Label(s.getName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Label statsLabel = new Label(String.format("Base Damage: %.1f  |  Cooldown: %.2fs  |  Mana Cost: %.0f", 
                    s.getBaseDamage(), s.getCooldown(), s.getManaCost()));
            statsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffd166;");

            Label descLabel = new Label(s.getUpgradeDescription());
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8ba2c4;");

            details.getChildren().addAll(nameLabel, statsLabel, descLabel);

            // Upgrade Button
            VBox actionBox = new VBox(4);
            actionBox.setAlignment(Pos.CENTER);
            int cost = s.getUpgradeCost();
            Button upgradeBtn = new Button(String.format("Upgrade (%dg)", cost));
            upgradeBtn.getStyleClass().addAll("button", "primary-button");
            upgradeBtn.setPrefWidth(140);

            upgradeBtn.setOnAction(e -> {
                boolean success = gameEngine.upgradeSpell(index);
                if (success) {
                    refreshUI();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough gold to upgrade this spell!");
                    alert.showAndWait();
                }
            });

            actionBox.getChildren().add(upgradeBtn);
            card.getChildren().addAll(badge, details, actionBox);
            spellCardsContainer.getChildren().add(card);
        }
    }

    private VBox createCrystalTabContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Nexus Crystal Aegis");
        title.getStyleClass().add("section-header");

        Label desc = new Label("Fortify the sacred crystal's durability and mend structural fractures.");
        desc.setStyle("-fx-text-fill: #8ba2c4; -fx-font-size: 13px;");

        HBox repairCard = new HBox(16);
        repairCard.getStyleClass().add("arcane-card");
        repairCard.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label repairTitle = new Label("Mend Crystal Fractures (+300 HP)");
        repairTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label repairSub = new Label("Restores 300 hit points to the Nexus Crystal.");
        repairSub.setStyle("-fx-text-fill: #8ba2c4;");
        info.getChildren().addAll(repairTitle, repairSub);

        Button repairBtn = new Button("Mend (40g)");
        repairBtn.getStyleClass().addAll("button", "primary-button");
        repairBtn.setOnAction(e -> {
            if (gameEngine.getPlayer().spendGold(40)) {
                gameEngine.getCrystal().heal(300);
                refreshUI();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Crystal restored by 300 HP!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough gold (requires 40g)!");
                alert.showAndWait();
            }
        });

        repairCard.getChildren().addAll(info, repairBtn);
        box.getChildren().addAll(title, desc, repairCard);
        return box;
    }

    private VBox createMageTabContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Elemental Mage Ascendance");
        title.getStyleClass().add("section-header");

        Label desc = new Label("Deepen your elemental conduit to expand max mana pool and regeneration speed.");
        desc.setStyle("-fx-text-fill: #8ba2c4; -fx-font-size: 13px;");

        HBox regenCard = new HBox(16);
        regenCard.getStyleClass().add("arcane-card");
        regenCard.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label regenTitle = new Label("Mana Well Amplification (+4 Mana/Sec & +25 Max Mana)");
        regenTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label regenSub = new Label("Permanently boosts mana regeneration and capacity.");
        regenSub.setStyle("-fx-text-fill: #8ba2c4;");
        info.getChildren().addAll(regenTitle, regenSub);

        Button buyBtn = new Button("Attune (60g)");
        buyBtn.getStyleClass().addAll("button", "primary-button");
        buyBtn.setOnAction(e -> {
            if (gameEngine.getPlayer().spendGold(60)) {
                gameEngine.getPlayer().setMaxMana(gameEngine.getPlayer().getMaxMana() + 25);
                gameEngine.getPlayer().setManaRegenRate(gameEngine.getPlayer().getManaRegenRate() + 4.0);
                gameEngine.getPlayer().restoreMana(25);
                refreshUI();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Mage conduit attuned! +4 Mana/sec, +25 Max Mana!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough gold (requires 60g)!");
                alert.showAndWait();
            }
        });

        regenCard.getChildren().addAll(info, buyBtn);
        box.getChildren().addAll(title, desc, regenCard);
        return box;
    }

    public void refreshUI() {
        goldHeaderLabel.setText("Gold: " + gameEngine.getPlayer().getGold() + "g");
        refreshSpellCards();
    }

    private String toHex(javafx.scene.paint.Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
