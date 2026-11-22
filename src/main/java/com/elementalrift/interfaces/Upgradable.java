package com.elementalrift.interfaces;

/**
 * Interface representing elements in the game that can be leveled up or upgraded in the shop.
 * [OOP Requirement: Interface 3 of 4]
 */
public interface Upgradable {
    /**
     * Increases the level and enhances stats (damage, cooldown, etc.)
     */
    void upgrade();

    /**
     * Returns the gold cost required for the next upgrade.
     * @return cost in gold
     */
    int getUpgradeCost();

    /**
     * Returns the current tier/level.
     * @return current level
     */
    int getLevel();

    /**
     * Summary of what the next upgrade provides.
     * @return description string
     */
    String getUpgradeDescription();
}
