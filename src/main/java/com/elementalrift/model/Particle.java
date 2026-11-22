package com.elementalrift.model;

import com.elementalrift.interfaces.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Particle used in visual particle systems (embers, water ripples, wind streaks, rock debris).
 * [OOP Requirement: Classes & Objects]
 * [OOP Requirement: Interfaces - implements Drawable]
 */
public class Particle implements Drawable {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private double size;
    private double life;
    private final double maxLife;
    private final Color color;
    private final boolean fading;

    public Particle(double x, double y, double vx, double vy, double size, double maxLife, Color color, boolean fading) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.maxLife = Math.max(0.05, maxLife);
        this.life = this.maxLife;
        this.color = color;
        this.fading = fading;
    }

    public void update(double delta) {
        x += vx * delta;
        y += vy * delta;
        life = Math.max(0, life - delta);
    }

    public boolean isDead() {
        return life <= 0;
    }

    public double getProgress() {
        return life / maxLife;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isDead()) return;

        double opacity = fading ? getProgress() : 1.0;
        Color renderColor = color.deriveColor(0, 1, 1, opacity);
        gc.setFill(renderColor);
        gc.fillOval(x - size / 2.0, y - size / 2.0, size, size);
    }
}
