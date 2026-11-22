package com.elementalrift.model;

import javafx.scene.paint.Color;

/**
 * Enumeration representing the primary elemental affinities in Elemental Rift.
 * Defines elemental counters and damage multiplier calculations:
 * - Water beats Fire (2.0x)
 * - Fire beats Air (2.0x)
 * - Air beats Earth (2.0x)
 * - Earth beats Water (2.0x)
 * Opposite matchup deals 0.5x damage, neutral deals 1.0x damage.
 */
public enum Element {
    FIRE("Fire", Color.web("#FF4500"), Color.web("#FFA500")),
    WATER("Water", Color.web("#1E90FF"), Color.web("#00BFFF")),
    AIR("Air", Color.web("#E0FFFF"), Color.web("#F0FFFF")),
    EARTH("Earth", Color.web("#8B4513"), Color.web("#CD853F")),
    NONE("Neutral", Color.web("#D3D3D3"), Color.web("#FFFFFF"));

    private final String displayName;
    private final Color primaryColor;
    private final Color secondaryColor;

    Element(String displayName, Color primaryColor, Color secondaryColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * Determines whether this element counters the target element.
     * @param target The target's elemental affinity.
     * @return true if this element has advantage over target.
     */
    public boolean beats(Element target) {
        if (target == null || target == NONE || this == NONE) {
            return false;
        }
        return (this == WATER && target == FIRE) ||
               (this == FIRE && target == AIR) ||
               (this == AIR && target == EARTH) ||
               (this == EARTH && target == WATER);
    }

    /**
     * Calculates the elemental damage multiplier based on attacker vs defender matchup.
     * [OOP Requirement: Method Design - calculateDamage & applyElementBonus]
     * @param attacker The attacking element (e.g. spell element)
     * @param defender The defending element (e.g. enemy element)
     * @return 2.0 if attacker counters defender, 0.5 if defender counters attacker, 1.0 otherwise.
     */
    public static double getDamageMultiplier(Element attacker, Element defender) {
        if (attacker == null || defender == null || attacker == NONE || defender == NONE) {
            return 1.0;
        }
        if (attacker.beats(defender)) {
            return 2.0; // Correct counter deals double damage
        }
        if (defender.beats(attacker)) {
            return 0.5; // Disadvantage deals half damage
        }
        return 1.0;     // Neutral matchup
    }
}
