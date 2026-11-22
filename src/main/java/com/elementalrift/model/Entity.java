package com.elementalrift.model;

import com.elementalrift.interfaces.Drawable;
import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base class for all dynamic combat participants in the Elemental Rift arena.
 * [OOP Requirement: Abstract Classes & Inheritance]
 * [OOP Requirement: Encapsulation - all fields private with controlled access methods]
 * [OOP Requirement: Interfaces - implements Drawable]
 */
public abstract class Entity implements Drawable {
    // Encapsulated state
    private double x;
    private double y;
    private double width;
    private double height;
    private double health;
    private double maxHealth;
    private double speed;
    private Element element;
    private boolean alive;

    /**
     * Default constructor initializing default entity values.
     * [OOP Requirement: Constructors - default constructor]
     */
    public Entity() {
        this(0, 0, 32, 32, 100, 100, Element.NONE);
    }

    /**
     * Parameterized constructor for entities with custom spatial and elemental attributes.
     * [OOP Requirement: Constructors - parameterized constructor]
     */
    public Entity(double x, double y, double width, double height, double health, double speed, Element element) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = Math.max(0, health);
        this.maxHealth = Math.max(1, health);
        this.speed = speed;
        this.element = (element != null) ? element : Element.NONE;
        this.alive = this.health > 0;
    }

    /**
     * Abstract update method to advance entity state by the elapsed time delta.
     * [OOP Requirement: Polymorphism - overridden by subclasses]
     * @param delta Seconds elapsed since the last frame.
     */
    public abstract void update(double delta);

    /**
     * Reduces the entity's health with controlled boundary checks.
     * [OOP Requirement: Encapsulation - controlled mutator method]
     * @param damage Amount of incoming damage to apply.
     * @return Actual damage dealt.
     */
    public double takeDamage(double damage) {
        if (!alive || damage <= 0) {
            return 0;
        }
        double actualDamage = Math.min(this.health, damage);
        this.health -= actualDamage;
        if (this.health <= 0) {
            this.health = 0;
            this.alive = false;
            onDeath();
        }
        return actualDamage;
    }

    /**
     * Restores entity health up to maxHealth.
     * @param amount Amount of healing.
     */
    public void heal(double amount) {
        if (!alive || amount <= 0) {
            return;
        }
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    /**
     * Hook method triggered when entity health drops to zero. Subclasses can override.
     */
    protected void onDeath() {
        // Default hook: no-op
    }

    // Getters and controlled setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getHealth() { return health; }
    public void setHealth(double health) { 
        this.health = Math.max(0, Math.min(maxHealth, health));
        this.alive = this.health > 0;
    }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public Element getElement() { return element; }
    public void setElement(Element element) { this.element = (element != null) ? element : Element.NONE; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    /**
     * Calculates Euclidean distance to another entity.
     * @param other Target entity.
     * @return Distance in arena units.
     */
    public double distanceTo(Entity other) {
        if (other == null) return Double.MAX_VALUE;
        double dx = this.getCenterX() - other.getCenterX();
        double dy = this.getCenterY() - other.getCenterY();
        return Math.hypot(dx, dy);
    }
}
