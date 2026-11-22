package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import com.elementalrift.interfaces.Castable;
import com.elementalrift.interfaces.Upgradable;
import java.util.List;

/**
 * Abstract class representing elemental spells wielded by the player.
 * [OOP Requirement: Abstract Classes & Inheritance - Spell -> FireSpell, WaterSpell, AirSpell, EarthSpell]
 * [OOP Requirement: Interfaces - Castable, Upgradable]
 * [OOP Requirement: Polymorphism - cast() and getEffect() overridden by subclasses]
 */
public abstract class Spell implements Castable, Upgradable {
    private final String name;
    private final Element element;
    private double baseDamage;
    private double manaCost;
    private double cooldown; // cooldown in seconds
    private double cooldownTimer; // remaining seconds until ready
    private int level;
    private String description;

    public Spell(String name, Element element, double baseDamage, double manaCost, double cooldown, String description) {
        this.name = name;
        this.element = element;
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.cooldownTimer = 0.0;
        this.level = 1;
        this.description = description;
    }

    /**
     * Decrements cooldown timer by delta seconds.
     */
    public void updateCooldown(double delta) {
        if (cooldownTimer > 0) {
            cooldownTimer = Math.max(0, cooldownTimer - delta);
        }
    }

    /**
     * Resets the cooldown timer to the full cooldown duration.
     */
    public void triggerCooldown() {
        this.cooldownTimer = this.cooldown;
    }

    @Override
    public boolean isReady() {
        return cooldownTimer <= 0.001;
    }

    /**
     * Validates cast conditions (mana & cooldown) and consumes mana.
     * [OOP Requirement: Exception Handling - throws custom exceptions]
     */
    protected void preCastCheck(Player player) throws NotEnoughManaException, SpellOnCooldownException {
        if (!isReady()) {
            throw new SpellOnCooldownException(this.name, this.cooldownTimer);
        }
        if (player.getMana() < this.manaCost) {
            throw new NotEnoughManaException(this.name, this.manaCost, player.getMana());
        }
        // Deduct mana and engage cooldown
        player.spendMana(this.manaCost);
        triggerCooldown();
    }

    /**
     * Subclasses define unique elemental secondary effects.
     * [OOP Requirement: Polymorphism]
     */
    public abstract String getEffectDescription();

    @Override
    public void upgrade() {
        this.level++;
        this.baseDamage *= 1.25;
        this.manaCost = Math.max(5.0, this.manaCost * 0.95);
        this.cooldown = Math.max(0.2, this.cooldown * 0.92);
    }

    @Override
    public int getUpgradeCost() {
        return 40 * level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getUpgradeDescription() {
        return String.format("Tier %d: +25%% Damage (%.1f -> %.1f), -8%% Cooldown (%.2fs -> %.2fs)",
                level + 1, baseDamage, baseDamage * 1.25, cooldown, cooldown * 0.92);
    }

    // Getters
    public String getName() { return name; }
    public Element getElement() { return element; }
    public double getBaseDamage() { return baseDamage; }
    public void setBaseDamage(double damage) { this.baseDamage = damage; }
    @Override
    public double getManaCost() { return manaCost; }
    public void setManaCost(double cost) { this.manaCost = cost; }
    public double getCooldown() { return cooldown; }
    public void setCooldown(double cooldown) { this.cooldown = cooldown; }
    public double getCooldownTimer() { return cooldownTimer; }
    public double getCooldownProgress() {
        if (cooldown <= 0) return 1.0;
        return 1.0 - (cooldownTimer / cooldown);
    }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
}
