package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import java.util.List;

/**
 * Water Elemental Spell: "Tidal Surge"
 * Fires a trident wave of 3 hydro-projectiles. Counters Fire (2x damage), weak against Earth (0.5x).
 * [OOP Requirement: Inheritance - Spell -> WaterSpell]
 * [OOP Requirement: Polymorphism - overrides cast() and getEffectDescription()]
 */
public class WaterSpell extends Spell {

    public WaterSpell() {
        super("Tidal Surge", Element.WATER, 38.0, 25.0, 1.0, 
              "Unleashes a sweeping trident arc of pressurized water waves.");
    }

    @Override
    public void cast(Player player, double targetX, double targetY, List<Projectile> projectiles)
            throws NotEnoughManaException, SpellOnCooldownException {
        preCastCheck(player);

        double originX = player.getCenterX();
        double originY = player.getCenterY();
        double dx = targetX - originX;
        double dy = targetY - originY;
        double baseAngle = Math.atan2(dy, dx);
        double speed = 440.0;

        // Spread angle of 14 degrees for trident spray
        double spread = Math.toRadians(14.0);
        double[] angles = {baseAngle - spread, baseAngle, baseAngle + spread};

        for (double angle : angles) {
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Projectile p = new Projectile(
                    originX, originY,
                    vx, vy,
                    getBaseDamage() * 0.75, 9.0,
                    Element.WATER, 1
            );
            projectiles.add(p);
        }
    }

    @Override
    public String getEffectDescription() {
        return "Trident spray slows enemies, dealing +100% bonus damage to Fire foes.";
    }
}
