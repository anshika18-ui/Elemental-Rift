package com.elementalrift.model;

import com.elementalrift.interfaces.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Projectile fired from cast spells travelling through the arena.
 * [OOP Requirement: Classes & Objects]
 * [OOP Requirement: Encapsulation - private speed, damage and pierce fields]
 * [OOP Requirement: Interfaces - implements Drawable]
 */
public class Projectile implements Drawable {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private final double damage;
    private final double radius;
    private final Element element;
    private int pierceRemaining;
    private boolean alive;
    private double rotation;

    public Projectile(double x, double y, double vx, double vy, double damage, double radius, Element element, int pierce) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.radius = radius;
        this.element = (element != null) ? element : Element.NONE;
        this.pierceRemaining = Math.max(1, pierce);
        this.alive = true;
        this.rotation = 0.0;
    }

    /**
     * Updates projectile position and rotational spin.
     */
    public void update(double delta) {
        x += vx * delta;
        y += vy * delta;
        rotation += delta * 12.0;

        // Auto-kill when out of arena bounds
        if (x < -100 || x > 1100 || y < -100 || y > 1000) {
            alive = false;
        }
    }

    /**
     * Consumes one pierce count upon hitting an enemy.
     * @return true if projectile should continue piercing, false if it dissipates.
     */
    public boolean registerHit() {
        pierceRemaining--;
        if (pierceRemaining <= 0) {
            alive = false;
            return false;
        }
        return true;
    }

    /**
     * Checks collision with circular entity target.
     * [OOP Requirement: Method Design - checkCollision]
     */
    public boolean collidesWith(Entity entity) {
        if (!alive || entity == null || !entity.isAlive()) return false;
        double dx = x - entity.getCenterX();
        double dy = y - entity.getCenterY();
        double combinedRadius = radius + (entity.getWidth() / 2.0);
        return (dx * dx + dy * dy) <= (combinedRadius * combinedRadius);
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDamage() { return damage; }
    public double getRadius() { return radius; }
    public Element getElement() { return element; }
    public int getPierceRemaining() { return pierceRemaining; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    @Override
    public void draw(GraphicsContext gc) {
        if (!alive) return;

        Color primary = element.getPrimaryColor();
        Color secondary = element.getSecondaryColor();

        // Glowing outer halo
        RadialGradient halo = new RadialGradient(
                0, 0, x, y, radius * 2.2, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, primary.deriveColor(0, 1, 1, 0.8)),
                new Stop(0.6, secondary.deriveColor(0, 1, 1, 0.3)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(halo);
        gc.fillOval(x - radius * 2.2, y - radius * 2.2, radius * 4.4, radius * 4.4);

        // Core visual style tailored per element
        switch (element) {
            case FIRE -> {
                // Fiery comet core
                gc.setFill(Color.WHITE);
                gc.fillOval(x - radius * 0.6, y - radius * 0.6, radius * 1.2, radius * 1.2);
                gc.setFill(primary);
                gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            }
            case WATER -> {
                // Hydro bubble with ring
                gc.setFill(primary.deriveColor(0, 1, 1, 0.7));
                gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                gc.setStroke(secondary);
                gc.setLineWidth(2.0);
                gc.strokeOval(x - radius * 1.3, y - radius * 1.3, radius * 2.6, radius * 2.6);
            }
            case AIR -> {
                // Gale lance elongated streak
                double angle = Math.atan2(vy, vx);
                gc.save();
                gc.translate(x, y);
                gc.rotate(Math.toDegrees(angle));
                gc.setFill(Color.WHITE);
                gc.fillPolygon(
                        new double[]{radius * 2.0, -radius * 1.5, -radius * 0.5, -radius * 1.5},
                        new double[]{0, -radius * 0.8, 0, radius * 0.8},
                        4
                );
                gc.restore();
            }
            case EARTH -> {
                // Spinning jagged boulder
                gc.save();
                gc.translate(x, y);
                gc.rotate(Math.toDegrees(rotation));
                gc.setFill(primary);
                gc.fillRect(-radius, -radius, radius * 2, radius * 2);
                gc.setStroke(secondary);
                gc.setLineWidth(2.0);
                gc.strokeRect(-radius, -radius, radius * 2, radius * 2);
                gc.restore();
            }
            default -> {
                gc.setFill(primary);
                gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            }
        }
    }
}
