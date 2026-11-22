package com.elementalrift.model;

import com.elementalrift.interfaces.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * The sacred Nexus Crystal the player must defend against oncoming enemy waves.
 * [OOP Requirement: Classes & Objects]
 * [OOP Requirement: Encapsulation - private health and visual state]
 * [OOP Requirement: Interfaces - implements Drawable]
 */
public class Crystal implements Drawable {
    private final double x;
    private final double y;
    private final double radius;
    private double health;
    private double maxHealth;
    private double pulsePhase; // for glowing breathing animation
    private boolean destroyed;

    public Crystal(double x, double y, double radius, double maxHealth) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.maxHealth = Math.max(10, maxHealth);
        this.health = this.maxHealth;
        this.pulsePhase = 0;
        this.destroyed = false;
    }

    /**
     * Default constructor placing the crystal at the bottom-center of the arena.
     */
    public Crystal() {
        this(400, 520, 36, 1000);
    }

    /**
     * Advances visual pulse animation.
     * @param delta Elapsed time.
     */
    public void update(double delta) {
        pulsePhase += delta * 3.0;
        if (pulsePhase > Math.PI * 2) {
            pulsePhase -= Math.PI * 2;
        }
    }

    /**
     * Applies damage to the crystal and calculates crack state.
     * [OOP Requirement: Encapsulation]
     * @param damage Amount of damage.
     * @return true if damage was applied.
     */
    public synchronized boolean takeDamage(double damage) {
        if (destroyed || damage <= 0) {
            return false;
        }
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            this.destroyed = true;
        }
        return true;
    }

    /**
     * Restores crystal health up to maximum.
     */
    public synchronized void heal(double amount) {
        if (destroyed || amount <= 0) return;
        this.health = Math.min(maxHealth, this.health + amount);
    }

    /**
     * Calculates the crack tier: 0 (pristine) to 4 (shattered).
     * @return int crack level 0-4.
     */
    public int getCrackLevel() {
        double ratio = getHealthRatio();
        if (ratio <= 0) return 4;
        if (ratio < 0.25) return 3;
        if (ratio < 0.50) return 2;
        if (ratio < 0.75) return 1;
        return 0;
    }

    public double getHealthRatio() {
        return Math.max(0, Math.min(1.0, health / maxHealth));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public synchronized double getHealth() { return health; }
    public synchronized double getMaxHealth() { return maxHealth; }
    public synchronized boolean isDestroyed() { return destroyed; }

    /**
     * Polymorphic rendering of the glowing, cracking crystal.
     */
    @Override
    public void draw(GraphicsContext gc) {
        double pulse = Math.sin(pulsePhase) * 4;
        double currentRadius = radius + pulse;
        double ratio = getHealthRatio();

        // Color shifts from pure cyan/magenta to ominous flickering amber/red as health drops
        Color coreColor = Color.rgb(
                (int) (50 + (1.0 - ratio) * 200),
                (int) (200 * ratio),
                (int) (255 * ratio)
        );

        // Outer ethereal glow
        RadialGradient glow = new RadialGradient(
                0, 0, x, y, currentRadius * 2.2, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, coreColor.deriveColor(0, 1, 1, 0.45)),
                new Stop(0.5, coreColor.deriveColor(0, 1, 1, 0.15)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(glow);
        gc.fillOval(x - currentRadius * 2.2, y - currentRadius * 2.2, currentRadius * 4.4, currentRadius * 4.4);

        // Sacred Pedestal Base
        gc.setFill(Color.rgb(30, 30, 45));
        gc.fillPolygon(
                new double[]{x - radius * 1.4, x, x + radius * 1.4, x + radius * 0.9, x - radius * 0.9},
                new double[]{y + radius * 0.8, y + radius * 1.5, y + radius * 0.8, y + radius * 0.4, y + radius * 0.4},
                5
        );
        gc.setStroke(Color.rgb(100, 120, 180));
        gc.setLineWidth(1.5);
        gc.strokePolygon(
                new double[]{x - radius * 1.4, x, x + radius * 1.4, x + radius * 0.9, x - radius * 0.9},
                new double[]{y + radius * 0.8, y + radius * 1.5, y + radius * 0.8, y + radius * 0.4, y + radius * 0.4},
                5
        );

        // Diamond-shaped faceted crystal core
        double[] xPoints = {x, x + currentRadius * 0.85, x, x - currentRadius * 0.85};
        double[] yPoints = {y - currentRadius * 1.25, y, y + currentRadius * 0.85, y};

        RadialGradient crystalGrad = new RadialGradient(
                0, 0, x, y - currentRadius * 0.3, currentRadius, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.WHITE),
                new Stop(0.3, coreColor),
                new Stop(1.0, coreColor.darker())
        );
        gc.setFill(crystalGrad);
        gc.fillPolygon(xPoints, yPoints, 4);

        // Facet lines
        gc.setStroke(Color.rgb(220, 240, 255, 0.7));
        gc.setLineWidth(1.5);
        gc.strokePolygon(xPoints, yPoints, 4);
        gc.strokeLine(x, y - currentRadius * 1.25, x, y + currentRadius * 0.85);
        gc.strokeLine(x - currentRadius * 0.85, y, x + currentRadius * 0.85, y);

        // Draw dynamic fractures as the crystal takes damage
        int crackLevel = getCrackLevel();
        if (crackLevel > 0) {
            gc.setStroke(Color.rgb(20, 10, 15, 0.85));
            gc.setLineWidth(2.0);
            // Crack 1
            gc.strokeLine(x - 5, y - currentRadius * 0.6, x + 8, y - 5);
            gc.strokeLine(x + 8, y - 5, x + 3, y + 15);
        }
        if (crackLevel > 1) {
            // Crack 2
            gc.strokeLine(x - currentRadius * 0.5, y - 2, x - 2, y + 8);
            gc.strokeLine(x - 2, y + 8, x - 12, y + 20);
        }
        if (crackLevel > 2) {
            // Crack 3 (deep fractures with fiery internal rupture)
            gc.setStroke(Color.rgb(255, 80, 40, 0.9));
            gc.setLineWidth(2.5);
            gc.strokeLine(x + 2, y - currentRadius * 0.9, x - 8, y - 10);
            gc.strokeLine(x - 8, y - 10, x + 15, y + 10);
        }
        if (crackLevel >= 4) {
            // Shattered sparks
            gc.setStroke(Color.RED);
            gc.setLineWidth(3.0);
            gc.strokeLine(x - 20, y - 20, x + 20, y + 20);
            gc.strokeLine(x + 20, y - 20, x - 20, y + 20);
        }
    }
}
