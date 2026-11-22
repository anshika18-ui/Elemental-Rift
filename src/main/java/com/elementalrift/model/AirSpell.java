package com.elementalrift.model;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import java.util.List;

/**
 * Air Elemental Spell: "Gale Lance"
 * Hyper-sonic wind vortex with piercing capability. Counters Earth (2x damage), weak against Fire (0.5x).
 * [OOP Requirement: Inheritance - Spell -> AirSpell]
 * [OOP Requirement: Polymorphism - overrides cast() and getEffectDescription()]
 */
public class AirSpell extends Spell {

    public AirSpell() {
        super("Gale Lance", Element.AIR, 42.0, 18.0, 0.5, 
              "Discharges an ultra-fast piercing vortex lance that slices through foes.");
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
            double speed = 650.0; // Very fast projectile
            // Pierce count of 3 enemies!
            Projectile p = new Projectile(
                    originX, originY,
                    dirX * speed, dirY * speed,
                    getBaseDamage(), 7.0,
                    Element.AIR, 3
            );
            projectiles.add(p);
        }
    }

    @Override
    public String getEffectDescription() {
        return "Pierces through up to 3 enemies, dealing +100% bonus damage to Earth foes.";
    }
}
