package com.elementalrift.exceptions;

/**
 * Custom Exception thrown when the player attempts to cast a spell with insufficient mana.
 * [OOP Requirement: Custom Exception 1 of 3]
 */
public class NotEnoughManaException extends Exception {
    private final double requiredMana;
    private final double currentMana;

    public NotEnoughManaException(String spellName, double requiredMana, double currentMana) {
        super(String.format("Cannot cast '%s': requires %.1f mana, but you only have %.1f mana!", 
                spellName, requiredMana, currentMana));
        this.requiredMana = requiredMana;
        this.currentMana = currentMana;
    }

    public double getRequiredMana() {
        return requiredMana;
    }

    public double getCurrentMana() {
        return currentMana;
    }
}
