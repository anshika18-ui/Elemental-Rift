package com.elementalrift.exceptions;

/**
 * Custom Exception thrown when casting a spell whose cooldown period has not yet elapsed.
 * [OOP Requirement: Custom Exception 2 of 3]
 */
public class SpellOnCooldownException extends Exception {
    private final double remainingCooldownSeconds;

    public SpellOnCooldownException(String spellName, double remainingCooldownSeconds) {
        super(String.format("Spell '%s' is on cooldown! %.2f seconds remaining.", 
                spellName, remainingCooldownSeconds));
        this.remainingCooldownSeconds = remainingCooldownSeconds;
    }

    public double getRemainingCooldownSeconds() {
        return remainingCooldownSeconds;
    }
}
