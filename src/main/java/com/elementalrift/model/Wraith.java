package com.elementalrift.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Wraith enemy: hovering ethereal specter that sways sinusoidally toward the crystal.
 * [OOP Requirement: Inheritance - Enemy -> Wraith]
 * [OOP Requirement: Polymorphism - overrides move, attack, draw]
 */
public class Wraith extends Enemy {
    private double swayPhase;

    public Wraith(double x, double y, Element element) {
        super(x, y, 32, 36, 90.0, 55.0, element, 14.0, 40.0, 1.5, 25, 80);
        this.swayPhase = Math.random() * Math.PI * 2;
    }

    @Override
    public void move(double delta, Crystal target) {
        if (target == null) return;
        swayPhase += delta * 4.0;
        double targetX = target.getX();
        double targetY = target.getY();
        double dx = targetX - getCenterX();
        double dy = targetY - getCenterY();
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            double dirX = dx / dist;
            double dirY = dy / dist;
            // Ethereal side-to-side sweeping
            double perpX = -dirY * Math.cos(swayPhase) * 0.8;
            double perpY = dirX * Math.cos(swayPhase) * 0.8;

            setX(getX() + (dirX + perpX) * getSpeed() * delta);
            setY(getY() + (dirY + perpY) * getSpeed() * delta);
        }
    }

    @Override
    public void attack(Crystal crystal) {
        if (crystal != null) {
            crystal.takeDamage(getAttackPower());
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY();
        double r = getWidth() / 2.0;

        drawHealthBar(gc);

        // Phantom mist aura
        RadialGradient mist = new RadialGradient(
                0, 0, cx, cy, r + 8, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, getElement().getPrimaryColor().deriveColor(0, 1, 1, 0.4)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(mist);
        gc.fillOval(cx - r - 8, cy - r - 8, (r + 8) * 2, (r + 8) * 2);

        // Ghost body
        if (isHitFlashing()) {
            gc.setFill(Color.WHITE);
        } else {
            gc.setFill(Color.rgb(20, 20, 30, 0.85));
        }
        gc.fillOval(cx - r, cy - r, getWidth(), getHeight() * 0.7);

        // Ethereal trailing tendrils
        double tOffset = Math.sin(swayPhase) * 5;
        gc.fillPolygon(
                new double[]{cx - r, cx - r * 0.4 + tOffset, cx, cx + r * 0.4 - tOffset, cx + r},
                new double[]{cy, cy + getHeight() * 0.5, cy + getHeight() * 0.35, cy + getHeight() * 0.5, cy},
                5
        );

        // Glowing elemental specter eyes
        gc.setFill(getElement().getSecondaryColor());
        gc.fillOval(cx - 6, cy - 3, 4, 4);
        gc.fillOval(cx + 2, cy - 3, 4, 4);
    }
}
