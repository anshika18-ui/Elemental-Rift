package com.elementalrift.model;

import com.elementalrift.interfaces.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Floating combat text showing damage numbers and elemental effectiveness.
 * [OOP Requirement: Classes & Objects]
 * [OOP Requirement: Interfaces - implements Drawable]
 */
public class DamageText implements Drawable {
    private final String text;
    private double x;
    private double y;
    private final Color color;
    private double life;
    private final double maxLife;
    private final boolean critical;

    public DamageText(String text, double x, double y, Color color, boolean critical) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.critical = critical;
        this.maxLife = critical ? 1.0 : 0.75;
        this.life = this.maxLife;
    }

    public void update(double delta) {
        y -= delta * 35.0; // float upwards
        life = Math.max(0, life - delta);
    }

    public boolean isDead() {
        return life <= 0;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isDead()) return;
        double progress = life / maxLife;
        gc.save();
        gc.setFont(Font.font("Segoe UI", critical ? FontWeight.BOLD : FontWeight.SEMI_BOLD, critical ? 18 : 14));
        gc.setFill(color.deriveColor(0, 1, 1, progress));
        gc.fillText(text, x, y);
        gc.restore();
    }
}
