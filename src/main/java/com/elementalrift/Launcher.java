package com.elementalrift;

/**
 * Main Launcher entry point.
 * Does NOT extend javafx.application.Application directly, which allows the JVM
 * to load JavaFX libraries directly from the classpath without requiring
 * special module-path flags on modern JDKs.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
