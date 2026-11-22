package com.elementalrift.interfaces;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface representing any visual game element that can be rendered onto the JavaFX Canvas.
 * [OOP Requirement: Interface 1 of 4]
 * Enables polymorphic rendering: List<Drawable> can be iterated and drawn without knowing concrete types.
 */
public interface Drawable {
    /**
     * Renders the entity or visual effect onto the given GraphicsContext.
     * @param gc The JavaFX GraphicsContext of the active arena canvas.
     */
    void draw(GraphicsContext gc);
}
