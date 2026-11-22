package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import java.util.List;

/**
 * Earth Elemental Spell: "Boulder Quake"
 * Heavy seismic rock missile. Counters Water (2x damage), weak against Air (0.5x).
 * [OOP Requirement: Inheritance - Spell -> EarthSpell]
 * [OOP Requirement: Polymorphism - overrides cast() and getEffectDescription()]
 */
public class EarthSpell extends Spell {

    public EarthSpell() {
        super("Boulder Quake", Element.EARTH, 85.0, 32.0, 1.4, 
              "Hurls a colossal seismic boulder that crushes enemies with immense force.");
    }

    @Override
    public void cast(Player player, double targetX, double targetY, List<Projectile> projectiles)
            throws NotEnoughManaException, SpellOnCooldownException {
        preCastCheck(player);

        double originX = player.getCenterX();
        double originY = player.getCenterY();
        double dx = targetX - originX;
        double dy = targetY - originY;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            double dirX = dx / dist;
            double dirY = dy / dist;
            double speed = 360.0;
            // Massive boulder with 2 pierce and heavy damage
            Projectile p = new Projectile(
                    originX, originY,
                    dirX * speed, dirY * speed,
                    getBaseDamage(), 15.0,
                    Element.EARTH, 2
            );
            projectiles.add(p);
        }
    }

    @Override
    public String getEffectDescription() {
        return "Crushes armor and pierces 2 enemies, dealing +100% bonus damage to Water foes.";
    }
}
