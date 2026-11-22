package com.elementalrift.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract class representing hostile elemental creatures advancing on the Nexus Crystal.
 * [OOP Requirement: Inheritance - extends Entity]
 * [OOP Requirement: Polymorphism - abstract methods move(), attack(), draw()]
 */
public abstract class Enemy extends Entity {
    private final double attackPower;
    private final double attackRange;
    private final double attackCooldown; // seconds between crystal strikes
    private double attackTimer;
    private final int goldReward;
    private final int scoreReward;
    private boolean attacking;
    private double hitFlashTimer; // Flash white when struck

    public Enemy(double x, double y, double width, double height, double health, double speed,
                 Element element, double attackPower, double attackRange, double attackCooldown,
                 int goldReward, int scoreReward) {
        super(x, y, width, height, health, speed, element);
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.attackTimer = 0.0;
        this.goldReward = goldReward;
        this.scoreReward = scoreReward;
        this.attacking = false;
        this.hitFlashTimer = 0.0;
    }

    /**
     * Updates movement towards the crystal, or attacks the crystal if in range.
     * [OOP Requirement: Polymorphism]
     * @param delta Elapsed time in seconds.
     * @param crystal Target nexus crystal.
     */
    public void updateEnemy(double delta, Crystal crystal) {
        if (!isAlive()) return;

        if (hitFlashTimer > 0) {
            hitFlashTimer -= delta;
        }

        if (crystal == null || crystal.isDestroyed()) {
            move(delta, null);
            return;
        }

        double dx = crystal.getX() - getCenterX();
        double dy = crystal.getY() - getCenterY();
        double dist = Math.hypot(dx, dy);

        if (dist <= attackRange) {
            attacking = true;
            attackTimer += delta;
            if (attackTimer >= attackCooldown) {
                attackTimer = 0.0;
                attack(crystal);
            }
        } else {
            attacking = false;
            move(delta, crystal);
        }
    }

    @Override
    public void update(double delta) {
        // Fallback if crystal not passed
        if (hitFlashTimer > 0) hitFlashTimer -= delta;
    }

    /**
     * Subclasses provide customized movement kinematics (e.g. Wraith weaves, Goblin dashes).
     * [OOP Requirement: Polymorphism]
     */
    public abstract void move(double delta, Crystal target);

    /**
     * Subclasses define their attack behaviour on the crystal.
     * [OOP Requirement: Polymorphism]
     */
    public abstract void attack(Crystal crystal);

    /**
     * Applies damage to enemy with elemental counter multiplier calculation.
     * [OOP Requirement: Method Design - calculateDamage & applyElementBonus]
     * @param spellDamage Raw damage from spell.
     * @param spellElement Elemental affinity of spell.
     * @return Actual damage applied.
     */
    public double takeElementalDamage(double spellDamage, Element spellElement) {
        double multiplier = Element.getDamageMultiplier(spellElement, this.getElement());
        double totalDamage = spellDamage * multiplier;
        takeDamage(totalDamage);
        this.hitFlashTimer = 0.15; // flash white
        return totalDamage;
    }

    /**
     * Draws an overhead health bar for the enemy.
     */
    protected void drawHealthBar(GraphicsContext gc) {
        double barWidth = getWidth();
        double barHeight = 4.0;
        double bx = getX();
        double by = getY() - 8.0;

        // Background
        gc.setFill(Color.rgb(40, 40, 40, 0.8));
        gc.fillRect(bx, by, barWidth, barHeight);

        // Fill
        double ratio = getHealth() / getMaxHealth();
        Color barColor = (ratio > 0.5) ? Color.LIMEGREEN : (ratio > 0.25) ? Color.GOLD : Color.CRIMSON;
        gc.setFill(barColor);
        gc.fillRect(bx, by, barWidth * ratio, barHeight);

        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.8);
        gc.strokeRect(bx, by, barWidth, barHeight);
    }

    // Getters
    public double getAttackPower() { return attackPower; }
    public double getAttackRange() { return attackRange; }
    public double getAttackCooldown() { return attackCooldown; }
    public int getGoldReward() { return goldReward; }
    public int getScoreReward() { return scoreReward; }
    public boolean isAttacking() { return attacking; }
    public boolean isHitFlashing() { return hitFlashTimer > 0; }
}
