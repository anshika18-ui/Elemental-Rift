package com.elementalrift.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Golem enemy: slow, heavily armored stone/metal monstrosity with high health.
 * [OOP Requirement: Inheritance - Enemy -> Golem]
 * [OOP Requirement: Polymorphism - overrides move, attack, draw]
 */
public class Golem extends Enemy {
    private double stompTimer;

    public Golem(double x, double y, Element element) {
        super(x, y, 44, 44, 220.0, 32.0, element, 22.0, 45.0, 2.0, 35, 120);
        this.stompTimer = 0;
    }

    @Override
    public void move(double delta, Crystal target) {
        if (target == null) return;
        stompTimer += delta * 4;
        double targetX = target.getX();
        double targetY = target.getY();
        double dx = targetX - getCenterX();
        double dy = targetY - getCenterY();
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            double dirX = dx / dist;
            double dirY = dy / dist;
            setX(getX() + dirX * getSpeed() * delta);
            setY(getY() + dirY * getSpeed() * delta);
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

        if (isHitFlashing()) {
            gc.setFill(Color.WHITE);
        } else {
            gc.setFill(Color.rgb(60, 50, 45));
        }

        // Bulky faceted armor plates
        gc.fillRect(cx - r, cy - r, getWidth(), getHeight());

        // Inner elemental core plates
        gc.setFill(getElement().getPrimaryColor());
        gc.fillRect(cx - r * 0.6, cy - r * 0.6, getWidth() * 0.6, getHeight() * 0.6);

        // Core runes
        gc.setStroke(getElement().getSecondaryColor());
        gc.setLineWidth(2.0);
        gc.strokeLine(cx - 6, cy, cx + 6, cy);
        gc.strokeLine(cx, cy - 6, cx, cy + 6);

        // Heavy shoulder guards
        gc.setFill(Color.rgb(40, 35, 30));
        gc.fillRect(cx - r - 4, cy - r, 6, 14);
        gc.fillRect(cx + r - 2, cy - r, 6, 14);
    }
}
