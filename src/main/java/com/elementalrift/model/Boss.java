package com.elementalrift.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Boss enemy: towering elemental titan appearing every few waves.
 * Triggers screen shake, possesses massive health and heavy crushing attacks.
 * [OOP Requirement: Inheritance - Enemy -> Boss]
 * [OOP Requirement: Polymorphism - overrides move, attack, draw]
 */
public class Boss extends Enemy {
    private final String bossTitle;
    private double auraPulse;
    private boolean enraged;

    public Boss(double x, double y, Element element, int waveNumber) {
        super(x, y, 64, 64, 600.0 + (waveNumber * 180.0), 25.0, element, 45.0, 60.0, 2.5, 120, 500);
        this.bossTitle = "Titan of the " + element.getDisplayName() + " Rift";
        this.auraPulse = 0.0;
        this.enraged = false;
    }

    @Override
    public void move(double delta, Crystal target) {
        if (target == null) return;
        auraPulse += delta * 5.0;

        // Check if enrage threshold reached (< 35% HP)
        if (!enraged && (getHealth() / getMaxHealth()) < 0.35) {
            enraged = true;
            setSpeed(getSpeed() * 1.5);
        }

        double dx = target.getX() - getCenterX();
        double dy = target.getY() - getCenterY();
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
            double damage = getAttackPower() * (enraged ? 1.5 : 1.0);
            crystal.takeDamage(damage);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY();
        double r = getWidth() / 2.0;

        // Boss health bar drawn wider above head with title
        drawBossHealthBar(gc);

        // Radiant pulsing elemental aura
        double pulseRadius = r + 15 + Math.sin(auraPulse) * 6;
        RadialGradient aura = new RadialGradient(
                0, 0, cx, cy, pulseRadius, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, enraged ? Color.rgb(255, 30, 0, 0.6) : getElement().getPrimaryColor().deriveColor(0, 1, 1, 0.5)),
                new Stop(0.7, getElement().getSecondaryColor().deriveColor(0, 1, 1, 0.2)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(aura);
        gc.fillOval(cx - pulseRadius, cy - pulseRadius, pulseRadius * 2, pulseRadius * 2);

        // Boss armored colossus body
        if (isHitFlashing()) {
            gc.setFill(Color.WHITE);
        } else {
            gc.setFill(Color.rgb(25, 20, 35));
        }
        gc.fillPolygon(
                new double[]{cx, cx + r, cx + r * 0.7, cx - r * 0.7, cx - r},
                new double[]{cy - r * 1.2, cy - r * 0.4, cy + r, cy + r, cy - r * 0.4},
                5
        );

        // Massive elemental crown
        gc.setFill(enraged ? Color.RED : getElement().getPrimaryColor());
        gc.fillPolygon(
                new double[]{cx - r * 0.8, cx - r * 0.4, cx, cx + r * 0.4, cx + r * 0.8},
                new double[]{cy - r * 0.9, cy - r * 1.4, cy - r * 1.1, cy - r * 1.4, cy - r * 0.9},
                5
        );

        // Intimidating glowing eyes
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 10, cy - r * 0.3, 6, 6);
        gc.fillOval(cx + 4, cy - r * 0.3, 6, 6);
        gc.setFill(enraged ? Color.RED : getElement().getSecondaryColor());
        gc.fillOval(cx - 9, cy - r * 0.3 + 1, 4, 4);
        gc.fillOval(cx + 5, cy - r * 0.3 + 1, 4, 4);
    }

    private void drawBossHealthBar(GraphicsContext gc) {
        double barWidth = getWidth() * 1.8;
        double barHeight = 8.0;
        double bx = getCenterX() - (barWidth / 2.0);
        double by = getY() - 20.0;

        // Background
        gc.setFill(Color.rgb(10, 10, 15, 0.9));
        gc.fillRect(bx, by, barWidth, barHeight);

        // Fill
        double ratio = getHealth() / getMaxHealth();
        Color barColor = enraged ? Color.CRIMSON : Color.ORANGERED;
        gc.setFill(barColor);
        gc.fillRect(bx, by, barWidth * ratio, barHeight);

        // Border
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(1.2);
        gc.strokeRect(bx, by, barWidth, barHeight);
    }

    public String getBossTitle() { return bossTitle; }
    public boolean isEnraged() { return enraged; }
}
