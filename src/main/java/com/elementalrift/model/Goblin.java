package com.elementalrift.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Goblin enemy: fast, nimble elemental skirmisher with lower health.
 * [OOP Requirement: Inheritance - Enemy -> Goblin]
 * [OOP Requirement: Polymorphism - overrides move, attack, draw]
 */
public class Goblin extends Enemy {
    private double bobbingTimer;

    public Goblin(double x, double y, Element element) {
        super(x, y, 26, 26, 60.0, 75.0, element, 8.0, 35.0, 1.2, 15, 50);
        this.bobbingTimer = Math.random() * Math.PI;
    }

    @Override
    public void move(double delta, Crystal target) {
        if (target == null) return;
        bobbingTimer += delta * 10;
        double targetX = target.getX();
        double targetY = target.getY();
        double dx = targetX - getCenterX();
        double dy = targetY - getCenterY();
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            double dirX = dx / dist;
            double dirY = dy / dist;
            // Add a small zigzag darting motion
            double perpX = -dirY * Math.sin(bobbingTimer) * 0.4;
            double perpY = dirX * Math.sin(bobbingTimer) * 0.4;
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

        // Flash white on hit
        if (isHitFlashing()) {
            gc.setFill(Color.WHITE);
        } else {
            gc.setFill(getElement().getPrimaryColor().darker());
        }
        // Slender pointed ears and body
        gc.fillOval(cx - r, cy - r, getWidth(), getHeight());

        // Goblin ears
        gc.setFill(getElement().getSecondaryColor());
        gc.fillPolygon(
                new double[]{cx - r, cx - r - 6, cx - r + 3},
                new double[]{cy - 3, cy - 10, cy - 2},
                3
        );
        gc.fillPolygon(
                new double[]{cx + r, cx + r + 6, cx + r - 3},
                new double[]{cy - 3, cy - 10, cy - 2},
                3
        );

        // Glowing elemental eyes
        gc.setFill(Color.YELLOW);
        gc.fillOval(cx - 5, cy - 3, 3, 3);
        gc.fillOval(cx + 2, cy - 3, 3, 3);
    }
}
