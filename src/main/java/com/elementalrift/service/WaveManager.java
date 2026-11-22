package com.elementalrift.service;

import com.elementalrift.model.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Manages enemy wave generation, escalating difficulty, and boss encounters.
 * [OOP Requirement: Method Design - separate responsibilities into spawnWave()]
 * [OOP Requirement: Polymorphism - produces polymorphic Enemy subclasses]
 */
public class WaveManager {
    private int currentWave;
    private final Queue<Enemy> spawnQueue;
    private double spawnInterval;
    private double spawnTimer;
    private boolean waveInProgress;
    private final Random random;

    public WaveManager() {
        this.currentWave = 0;
        this.spawnQueue = new LinkedList<>();
        this.spawnInterval = 1.2;
        this.spawnTimer = 0.0;
        this.waveInProgress = false;
        this.random = new Random();
    }

    /**
     * Prepares and populates the enemy queue for the next wave.
     * Spawns a Boss every 3 waves (Waves 3, 6, 9, etc.).
     * [OOP Requirement: Method Design]
     */
    public synchronized void startNextWave() {
        currentWave++;
        waveInProgress = true;
        spawnQueue.clear();
        spawnInterval = Math.max(0.45, 1.3 - (currentWave * 0.08));

        int enemyCount = 6 + (currentWave * 3);
        boolean isBossWave = (currentWave % 3 == 0);

        Element[] elements = {Element.FIRE, Element.WATER, Element.AIR, Element.EARTH};

        for (int i = 0; i < enemyCount; i++) {
            double spawnX = 50 + random.nextDouble() * 700;
            double spawnY = -20 - (random.nextDouble() * 150);
            Element element = elements[random.nextInt(elements.length)];

            int enemyType = random.nextInt(3);
            Enemy enemy;
            switch (enemyType) {
                case 0 -> enemy = new Goblin(spawnX, spawnY, element);
                case 1 -> enemy = new Golem(spawnX, spawnY, element);
                default -> enemy = new Wraith(spawnX, spawnY, element);
            }
            // Scale enemy HP slightly with wave progression
            enemy.setMaxHealth(enemy.getMaxHealth() * (1.0 + (currentWave - 1) * 0.15));
            enemy.setHealth(enemy.getMaxHealth());
            spawnQueue.add(enemy);
        }

        // Append Boss at the end of boss wave
        if (isBossWave) {
            Element bossElement = elements[(currentWave / 3) % elements.length];
            Boss boss = new Boss(360, -60, bossElement, currentWave);
            spawnQueue.add(boss);
        }
    }

    /**
     * Checks if a new enemy is ready to be spawned into the active arena.
     * @param delta Elapsed time.
     * @return Enemy to add to arena, or null if interval hasn't elapsed.
     */
    public synchronized Enemy pollNextSpawn(double delta) {
        if (!waveInProgress || spawnQueue.isEmpty()) {
            return null;
        }

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0.0;
            return spawnQueue.poll();
        }
        return null;
    }

    public synchronized boolean isSpawnQueueEmpty() {
        return spawnQueue.isEmpty();
    }

    public synchronized void setWaveInProgress(boolean inProgress) {
        this.waveInProgress = inProgress;
    }

    public synchronized boolean isWaveInProgress() {
        return waveInProgress;
    }

    public synchronized int getCurrentWave() {
        return currentWave;
    }

    public synchronized int getRemainingInQueue() {
        return spawnQueue.size();
    }

    public boolean isCurrentWaveBossWave() {
        return currentWave > 0 && (currentWave % 3 == 0);
    }
}
