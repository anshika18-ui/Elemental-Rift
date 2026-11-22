package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import java.util.List;

/**
 * Fire Elemental Spell: "Inferno Blast"
 * High destructive power, counters Air (2x damage), weak against Water (0.5x).
 * [OOP Requirement: Inheritance - Spell -> FireSpell]
 * [OOP Requirement: Polymorphism - overrides cast() and getEffectDescription()]
 */
public class FireSpell extends Spell {

    public FireSpell() {
        super("Inferno Blast", Element.FIRE, 55.0, 20.0, 0.75, 
              "Hurls a blazing pyro-sphere that explodes into ember particles.");
    }

    @Override
    public void cast(Player player, double targetX, double targetY, List<Projectile> projectiles)
            throws NotEnoughManaException, SpellOnCooldownException {
        // Enforces mana and cooldown checks
        preCastCheck(player);

        double originX = player.getCenterX();
        double originY = player.getCenterY();
        double dx = targetX - originX;
        double dy = targetY - originY;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            double dirX = dx / dist;
            double dirY = dy / dist;
            double speed = 480.0;
            // Create Fire projectile
            Projectile p = new Projectile(
                    originX, originY,
                    dirX * speed, dirY * speed,
                    getBaseDamage(), 10.0,
                    Element.FIRE, 1
            );
            projectiles.add(p);
        }
    }

    @Override
    public String getEffectDescription() {
        return "Ignites targets, dealing +100% bonus damage to Air foes.";
    }
}
