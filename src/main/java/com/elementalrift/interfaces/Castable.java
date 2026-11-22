package com.elementalrift.interfaces;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import com.elementalrift.model.Player;
import com.elementalrift.model.Projectile;
import java.util.List;

/**
 * Interface representing spell-casting behavior.
 * [OOP Requirement: Interface 2 of 4]
 * Decouples spell execution mechanics from concrete spell implementations.
 */
public interface Castable {
    /**
     * Executes the spell cast, creating projectiles and consuming resources.
     * @param player The caster casting the spell
     * @param targetX The target X coordinate clicked or aimed
     * @param targetY The target Y coordinate clicked or aimed
     * @param projectiles The game's active projectile list
     * @throws NotEnoughManaException when player's mana is insufficient
     * @throws SpellOnCooldownException when spell cooldown hasn't elapsed
     */
    void cast(Player player, double targetX, double targetY, List<Projectile> projectiles)
            throws NotEnoughManaException, SpellOnCooldownException;

    /**
     * Retrieves the current mana cost of the castable object.
     * @return mana cost as a double
     */
    double getManaCost();

    /**
     * Checks if the spell cooldown has elapsed.
     * @return true if ready to cast
     */
    boolean isReady();
}
