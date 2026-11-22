package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the playable Elemental Mage defending the arena crystal.
 * [OOP Requirement: Inheritance - extends Entity]
 * [OOP Requirement: Encapsulation - private fields with strict validation methods]
 * [OOP Requirement: Polymorphism - overrides update() and draw()]
 */
public class Player extends Entity {
    private double mana;
    private double maxMana;
    private double manaRegenRate; // mana per second
    private int gold;
    private int score;
    private int selectedSpellIndex;
    private final List<Spell> spells;
    private int totalSpellsCast;
    private int enemiesDefeated;
    private double aimAngle; // Radians toward cursor

    /**
     * Default constructor for the elemental mage.
     */
    public Player() {
        this(400, 500);
    }

    /**
     * Parameterized constructor specifying player's starting position.
     */
    public Player(double startX, double startY) {
        super(startX - 20, startY - 20, 40, 40, 200, 150, Element.NONE);
        this.maxMana = 150.0;
        this.mana = 150.0;
        this.manaRegenRate = 12.0; // Regenerates 12 mana/sec by default
        this.gold = 50;
        this.score = 0;
        this.spells = new ArrayList<>();
        this.selectedSpellIndex = 0;
        this.totalSpellsCast = 0;
        this.enemiesDefeated = 0;
        this.aimAngle = -Math.PI / 2.0;
    }

    /**
     * Updates player state, passive mana regeneration, and cooldowns.
     * @param delta Seconds elapsed.
     */
    @Override
    public void update(double delta) {
        // Natural mana regeneration
        restoreMana(manaRegenRate * delta);

        // Update spell cooldowns
        for (Spell spell : spells) {
            spell.updateCooldown(delta);
        }
    }

    /**
     * Deducts mana with strict bounds check.
     * [OOP Requirement: Encapsulation & Custom Exception Handling]
     * @param amount Mana required for action.
     * @throws NotEnoughManaException when mana is insufficient.
     */
    public synchronized void spendMana(double amount) throws NotEnoughManaException {
        if (amount < 0) return;
        if (this.mana < amount) {
            String spellName = (selectedSpellIndex >= 0 && selectedSpellIndex < spells.size()) 
                    ? spells.get(selectedSpellIndex).getName() : "Elemental Spell";
            throw new NotEnoughManaException(spellName, amount, this.mana);
        }
        this.mana -= amount;
        this.totalSpellsCast++;
    }

    /**
     * Safely restores player mana up to maxMana.
     * Protected with synchronized for multithreaded regeneration.
     */
    public synchronized void restoreMana(double amount) {
        if (amount <= 0) return;
        this.mana = Math.min(this.maxMana, this.mana + amount);
    }

    /**
     * Adds gold earned from defeated enemies or wave completions.
     */
    public synchronized void addGold(int amount) {
        if (amount > 0) {
            this.gold += amount;
        }
    }

    /**
     * Spends gold on spell upgrades or repairs.
     * @param amount Gold cost.
     * @return true if player had enough gold and purchase completed.
     */
    public synchronized boolean spendGold(int amount) {
        if (amount > 0 && this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    /**
     * Increments score from combat and wave milestones.
     */
    public synchronized void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    public synchronized void incrementKillCount() {
        this.enemiesDefeated++;
    }

    /**
     * Adds a spell to the mage's spell inventory.
     */
    public void addSpell(Spell spell) {
        if (spell != null && !spells.contains(spell)) {
            spells.add(spell);
        }
    }

    /**
     * Selects active spell by 0-based index.
     */
    public void selectSpell(int index) {
        if (index >= 0 && index < spells.size()) {
            this.selectedSpellIndex = index;
        }
    }

    public Spell getSelectedSpell() {
        if (spells.isEmpty()) return null;
        if (selectedSpellIndex < 0 || selectedSpellIndex >= spells.size()) {
            selectedSpellIndex = 0;
        }
        return spells.get(selectedSpellIndex);
    }

    // Getters and setters
    public synchronized double getMana() { return mana; }
    public synchronized void setMana(double mana) { this.mana = Math.max(0, Math.min(maxMana, mana)); }
    public synchronized double getMaxMana() { return maxMana; }
    public synchronized void setMaxMana(double maxMana) { this.maxMana = Math.max(10, maxMana); }
    public synchronized double getManaRegenRate() { return manaRegenRate; }
    public synchronized void setManaRegenRate(double rate) { this.manaRegenRate = Math.max(0, rate); }
    public synchronized int getGold() { return gold; }
    public synchronized int getScore() { return score; }
    public int getSelectedSpellIndex() { return selectedSpellIndex; }
    public List<Spell> getSpells() { return spells; }
    public int getTotalSpellsCast() { return totalSpellsCast; }
    public int getEnemiesDefeated() { return enemiesDefeated; }
    public double getAimAngle() { return aimAngle; }
    public void setAimAngle(double angle) { this.aimAngle = angle; }

    /**
     * Polymorphic rendering of the Player on the JavaFX Canvas.
     * [OOP Requirement: Polymorphism & Interfaces - Drawable]
     */
    @Override
    public void draw(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY();
        double r = getWidth() / 2.0;

        // Mage aura ring
        RadialGradient aura = new RadialGradient(
                0, 0, cx, cy, r + 10, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(100, 180, 255, 0.5)),
                new Stop(0.7, Color.rgb(50, 120, 220, 0.2)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(aura);
        gc.fillOval(cx - (r + 10), cy - (r + 10), (r + 10) * 2, (r + 10) * 2);

        // Mage body (robed circle)
        gc.setFill(Color.rgb(35, 45, 80));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Inner glowing crest matching selected spell element
        Spell currentSpell = getSelectedSpell();
        Color crestColor = (currentSpell != null) ? currentSpell.getElement().getPrimaryColor() : Color.WHITE;
        gc.setFill(crestColor);
        gc.fillOval(cx - (r * 0.5), cy - (r * 0.5), r, r);

        // Mage wizard hood / pointer facing aim direction
        double pointerLength = r + 8;
        double px = cx + Math.cos(aimAngle) * pointerLength;
        double py = cy + Math.sin(aimAngle) * pointerLength;
        gc.setStroke(crestColor);
        gc.setLineWidth(3.0);
        gc.strokeLine(cx, cy, px, py);

        // Mage border
        gc.setStroke(Color.rgb(180, 200, 255));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
    }
}
