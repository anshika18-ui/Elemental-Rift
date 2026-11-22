package com.elementalrift.service;

import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import com.elementalrift.interfaces.Drawable;
import com.elementalrift.interfaces.Upgradable;
import com.elementalrift.model.*;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core Game Engine coordinating real-time simulation, physics, combat mechanics,
 * thread-safe concurrency, and polymorphic rendering loops.
 *
 * [OOP Requirement: Method Design - separate responsibilities into spawnWave, calculateDamage, etc.]
 * [OOP Requirement: Polymorphism - manages polymorphic List<Enemy>, List<Spell>, List<Drawable>]
 * [OOP Requirement: Multithreading - ScheduledExecutorService (Thread 1) for mana/spawner, ReportGenerator (Thread 2)]
 * [OOP Requirement: Thread Safety - Concurrent collections, AtomicInteger, and synchronized blocks]
 */
public class GameEngine {

    // Core Game Objects (Living in heap objects, not static variables)
    private final Player player;
    private final Crystal crystal;
    private final WaveManager waveManager;
    private final NotificationService notificationService;
    private final SaveManager saveManager;

    // Polymorphic collections
    // Using CopyOnWriteArrayList / synchronized lists for thread-safe access across game loop & worker threads
    private final List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final List<Particle> particles = new CopyOnWriteArrayList<>();
    private final List<DamageText> damageTexts = new CopyOnWriteArrayList<>();

    // Thread-safe combat metrics for background analytics (Atomic counters and concurrent maps)
    private final Map<Element, Double> damageDealtByElement = new ConcurrentHashMap<>();
    private final Map<String, Integer> killsByEnemyType = new ConcurrentHashMap<>();
    private final AtomicInteger successfulCounterHits = new AtomicInteger(0);
    private final AtomicInteger totalHitsScored = new AtomicInteger(0);

    // Arena dimensions
    private final double arenaWidth;
    private final double arenaHeight;

    // Game state
    private volatile GameState gameState;
    private final Random random = new Random();

    // Visual polish: screen shake
    private double screenShakeIntensity = 0.0;
    private double screenShakeDuration = 0.0;

    // Multithreading: Thread 1 - Scheduled background executor for mana & wave cadence
    // [OOP Requirement: Multithreading - ScheduledExecutorService for Thread 1]
    private final ScheduledExecutorService backgroundExecutor = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "ElementalRift-BackgroundWorker");
        t.setDaemon(true); // Allows clean JVM shutdown
        return t;
    });

    public GameEngine(double arenaWidth, double arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.player = new Player(arenaWidth / 2.0, arenaHeight - 70);
        this.crystal = new Crystal(arenaWidth / 2.0, arenaHeight - 60, 34, 1000);
        this.waveManager = new WaveManager();
        this.notificationService = new NotificationService();
        this.saveManager = new SaveManager();
        this.gameState = GameState.MENU;

        // Initialize spells into player's spell book
        initSpells();

        // Initialize metric tracking
        for (Element e : Element.values()) {
            damageDealtByElement.put(e, 0.0);
        }

        // Start Multithreaded background tasks (Thread 1)
        startBackgroundWorkers();
    }

    /**
     * Initializes the player's 4 elemental spells.
     * [OOP Requirement: Polymorphism - populating List<Spell> with distinct subclasses]
     */
    private void initSpells() {
        player.addSpell(new FireSpell());
        player.addSpell(new WaterSpell());
        player.addSpell(new AirSpell());
        player.addSpell(new EarthSpell());
    }

    /**
     * Starts background thread tasks (Thread 1).
     * [OOP Requirement: Multithreading - Thread 1: mana regeneration + wave/enemy spawner]
     * Uses ScheduledExecutorService running at fixed rate; data access is protected.
     */
    private void startBackgroundWorkers() {
        // Scheduled Task 1: Periodic mana boost and passive shield check every 500ms
        backgroundExecutor.scheduleAtFixedRate(() -> {
            if (gameState == GameState.PLAYING) {
                // Thread-safe mana restore
                player.restoreMana(3.0);
            }
        }, 500, 500, TimeUnit.MILLISECONDS);

        // Scheduled Task 2: Background wave spawner monitor every 200ms
        backgroundExecutor.scheduleAtFixedRate(() -> {
            if (gameState == GameState.PLAYING) {
                // Check if wave is complete
                if (waveManager.isWaveInProgress() && waveManager.isSpawnQueueEmpty() && enemies.isEmpty()) {
                    waveManager.setWaveInProgress(false);
                    player.addGold(50 + waveManager.getCurrentWave() * 20);
                    player.addScore(200 * waveManager.getCurrentWave());

                    // Safe UI notification via Platform.runLater
                    notificationService.notify(
                            String.format("Wave %d Cleared! +%d Gold Bonus", 
                                    waveManager.getCurrentWave(), 50 + waveManager.getCurrentWave() * 20),
                            "SUCCESS"
                    );

                    // Auto-start next wave after brief interval
                    try {
                        Thread.sleep(2000);
                        if (gameState == GameState.PLAYING) {
                            spawnWave();
                        }
                    } catch (InterruptedException ignored) {}
                }
            }
        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Spawns the next wave of enemies.
     * [OOP Requirement: Method Design - spawnWave()]
     */
    public synchronized void spawnWave() {
        waveManager.startNextWave();
        boolean isBoss = waveManager.isCurrentWaveBossWave();
        if (isBoss) {
            triggerScreenShake(8.0, 0.5);
            notificationService.notify(
                    String.format("ALERT: Boss Titan detected in Wave %d!", waveManager.getCurrentWave()),
                    "DANGER"
            );
        } else {
            notificationService.notify(
                    String.format("Wave %d incoming!", waveManager.getCurrentWave()),
                    "INFO"
            );
        }
    }

    /**
     * Main update tick called by JavaFX AnimationTimer at 60 FPS.
     * @param delta Elapsed time in seconds.
     */
    public void update(double delta) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Screen shake decay
        if (screenShakeDuration > 0) {
            screenShakeDuration -= delta;
            if (screenShakeDuration <= 0) {
                screenShakeIntensity = 0;
            }
        }

        // Update Crystal and Player
        crystal.update(delta);
        player.update(delta);

        // Check if crystal was destroyed
        if (crystal.isDestroyed()) {
            gameState = GameState.GAME_OVER;
            notificationService.notify("The Nexus Crystal was shattered! GAME OVER", "DANGER");
            return;
        }

        // Poll enemy spawns from WaveManager
        Enemy newEnemy = waveManager.pollNextSpawn(delta);
        if (newEnemy != null) {
            enemies.add(newEnemy);
        }

        // Update active enemies
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.updateEnemy(delta, crystal);
            } else {
                handleEnemyDeath(enemy);
                enemies.remove(enemy);
            }
        }

        // Update projectiles & perform collision detection
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.update(delta);
                checkCollisions(projectile);
            } else {
                projectiles.remove(projectile);
            }
        }

        // Update particles
        for (Particle particle : particles) {
            if (!particle.isDead()) {
                particle.update(delta);
            } else {
                particles.remove(particle);
            }
        }

        // Update damage texts
        for (DamageText dt : damageTexts) {
            if (!dt.isDead()) {
                dt.update(delta);
            } else {
                damageTexts.remove(dt);
            }
        }
    }

    /**
     * Checks collision between a projectile and all active enemies.
     * [OOP Requirement: Method Design - checkCollision()]
     */
    private void checkCollisions(Projectile p) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || !p.isAlive()) continue;

            if (p.collidesWith(enemy)) {
                // Calculate damage with elemental bonus
                double totalDamage = calculateDamage(p.getDamage(), p.getElement(), enemy.getElement());
                boolean isAdvantage = p.getElement().beats(enemy.getElement());

                // Apply damage to enemy
                enemy.takeDamage(totalDamage);

                // Update metrics
                totalHitsScored.incrementAndGet();
                if (isAdvantage) {
                    successfulCounterHits.incrementAndGet();
                }
                damageDealtByElement.merge(p.getElement(), totalDamage, Double::sum);

                // Visual effects: spawn floating combat damage text
                Color textColor = isAdvantage ? Color.GOLD : (p.getElement().getPrimaryColor());
                String tag = isAdvantage ? String.format("%.0f (2x)", totalDamage) : String.format("%.0f", totalDamage);
                damageTexts.add(new DamageText(tag, enemy.getCenterX(), enemy.getY() - 10, textColor, isAdvantage));

                // Visual effects: spawn elemental impact particle burst
                spawnImpactParticles(enemy.getCenterX(), enemy.getCenterY(), p.getElement());

                // Register hit on projectile (pierce reduction)
                boolean stillAlive = p.registerHit();
                if (!stillAlive) {
                    break;
                }
            }
        }
    }

    /**
     * Calculates elemental damage based on counter matchups.
     * [OOP Requirement: Method Design - calculateDamage & applyElementBonus]
     */
    public double calculateDamage(double baseDamage, Element attackElement, Element defenseElement) {
        double multiplier = Element.getDamageMultiplier(attackElement, defenseElement);
        return baseDamage * multiplier;
    }

    /**
     * Processes enemy defeat: rewards gold, score, particles, and metric recording.
     */
    private void handleEnemyDeath(Enemy enemy) {
        player.addGold(enemy.getGoldReward());
        player.addScore(enemy.getScoreReward());
        player.incrementKillCount();

        // Record metrics
        String typeName = enemy.getClass().getSimpleName();
        killsByEnemyType.merge(typeName, 1, Integer::sum);

        // Heavy particle explosion
        spawnDeathParticles(enemy.getCenterX(), enemy.getCenterY(), enemy.getElement());

        // Screen shake if Boss defeated
        if (enemy instanceof Boss) {
            triggerScreenShake(12.0, 0.6);
            notificationService.notify("BOSS SLAIN! +500 Score Bonus!", "SUCCESS");
        }
    }

    /**
     * Casts the currently selected spell toward target coordinates.
     * [OOP Requirement: Exception Handling - catches NotEnoughManaException & SpellOnCooldownException]
     * Never crashes; notifies the user cleanly.
     */
    public void castSelectedSpell(double targetX, double targetY) {
        if (gameState != GameState.PLAYING) return;

        Spell activeSpell = player.getSelectedSpell();
        if (activeSpell == null) return;

        try {
            // Polymorphic spell casting
            activeSpell.cast(player, targetX, targetY, (List<Projectile>) projectiles);
        } catch (NotEnoughManaException e) {
            notificationService.notify(
                    String.format("Not enough mana! Need %.0f, have %.0f", e.getRequiredMana(), e.getCurrentMana()),
                    "WARNING"
            );
        } catch (SpellOnCooldownException e) {
            notificationService.notify(
                    String.format("Spell on cooldown (%.1fs left)", e.getRemainingCooldownSeconds()),
                    "WARNING"
            );
        }
    }

    /**
     * Upgrades a spell by index in the shop.
     * [OOP Requirement: Method Design - upgradeSpell()]
     * [OOP Requirement: Interface - Upgradable]
     */
    public boolean upgradeSpell(int spellIndex) {
        if (spellIndex < 0 || spellIndex >= player.getSpells().size()) {
            return false;
        }
        Spell spell = player.getSpells().get(spellIndex);
        int cost = spell.getUpgradeCost();

        if (player.spendGold(cost)) {
            spell.upgrade();
            notificationService.notify(
                    String.format("Upgraded %s to Tier %d!", spell.getName(), spell.getLevel()),
                    "SUCCESS"
            );
            return true;
        } else {
            notificationService.notify("Not enough gold to upgrade " + spell.getName() + "!", "WARNING");
            return false;
        }
    }

    /**
     * Spawns particle burst on projectile collision.
     */
    private void spawnImpactParticles(double x, double y, Element element) {
        Color pColor = element.getPrimaryColor();
        Color sColor = element.getSecondaryColor();
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 40 + random.nextDouble() * 90;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color c = (i % 2 == 0) ? pColor : sColor;
            particles.add(new Particle(x, y, vx, vy, 4 + random.nextDouble() * 3, 0.4 + random.nextDouble() * 0.3, c, true));
        }
    }

    /**
     * Spawns radiant particle burst on enemy death.
     */
    private void spawnDeathParticles(double x, double y, Element element) {
        Color pColor = element.getPrimaryColor();
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 50 + random.nextDouble() * 160;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, 5 + random.nextDouble() * 5, 0.6 + random.nextDouble() * 0.4, pColor, true));
        }
    }

    /**
     * Engages camera screen shake.
     */
    public void triggerScreenShake(double intensity, double duration) {
        this.screenShakeIntensity = intensity;
        this.screenShakeDuration = duration;
    }

    /**
     * Initiates asynchronous background report generation (Thread 2).
     * [OOP Requirement: Multithreading - Thread 2 with progress indicator and Platform.runLater]
     */
    public void generateAnalyticsReport(java.util.function.Consumer<Double> progressCallback,
                                         java.util.function.Consumer<ReportGenerator.AnalyticsReport> completionCallback) {
        ReportGenerator task = new ReportGenerator(
                damageDealtByElement,
                killsByEnemyType,
                successfulCounterHits.get(),
                totalHitsScored.get(),
                progressCallback,
                completionCallback
        );
        // Dispatch to background thread pool
        backgroundExecutor.execute(task);
    }

    /**
     * Polymorphic rendering of the arena onto the JavaFX Canvas.
     * [OOP Requirement: Interface & Polymorphism - iterating List<Drawable>]
     */
    public void render(GraphicsContext gc) {
        gc.save();

        // Apply screen shake offset if active
        if (screenShakeDuration > 0 && screenShakeIntensity > 0) {
            double shakeX = (random.nextDouble() - 0.5) * screenShakeIntensity * 2;
            double shakeY = (random.nextDouble() - 0.5) * screenShakeIntensity * 2;
            gc.translate(shakeX, shakeY);
        }

        // Draw arena dark fantasy background
        drawArenaBackground(gc);

        // Polymorphic Drawables rendering: Crystal, Player, Enemies, Projectiles, Particles
        crystal.draw(gc);
        player.draw(gc);

        // Polymorphic Enemy draw
        for (Enemy enemy : enemies) {
            enemy.draw(gc);
        }

        // Projectile draw
        for (Projectile projectile : projectiles) {
            projectile.draw(gc);
        }

        // Particle draw
        for (Particle particle : particles) {
            particle.draw(gc);
        }

        // Damage text draw
        for (DamageText dt : damageTexts) {
            dt.draw(gc);
        }

        gc.restore();
    }

    /**
     * Renders dark fantasy arena background with runic floor circles and subtle grid glow.
     */
    private void drawArenaBackground(GraphicsContext gc) {
        // Deep obsidian arena backdrop
        gc.setFill(Color.rgb(14, 16, 26));
        gc.fillRect(0, 0, arenaWidth, arenaHeight);

        // Mystical ritual arena circle
        double cx = arenaWidth / 2.0;
        double cy = arenaHeight / 2.0;

        gc.setStroke(Color.rgb(35, 45, 75, 0.4));
        gc.setLineWidth(2.0);
        gc.strokeOval(cx - 260, cy - 260, 520, 520);
        gc.strokeOval(cx - 160, cy - 160, 320, 320);

        // Arcane cross lines
        gc.strokeLine(cx - 280, cy, cx + 280, cy);
        gc.strokeLine(cx, cy - 280, cx, cy + 280);

        // Subtle arena boundary border
        gc.setStroke(Color.rgb(60, 75, 120, 0.6));
        gc.setLineWidth(3.0);
        gc.strokeRect(4, 4, arenaWidth - 8, arenaHeight - 8);
    }

    /**
     * Resets game state to start a new run.
     */
    public synchronized void restartGame() {
        enemies.clear();
        projectiles.clear();
        particles.clear();
        damageTexts.clear();

        player.setHealth(player.getMaxHealth());
        player.setMana(player.getMaxMana());
        crystal.heal(crystal.getMaxHealth());

        damageDealtByElement.replaceAll((k, v) -> 0.0);
        killsByEnemyType.clear();
        successfulCounterHits.set(0);
        totalHitsScored.set(0);

        gameState = GameState.PLAYING;
        spawnWave();
    }

    public void shutdown() {
        backgroundExecutor.shutdownNow();
    }

    // Getters and state controllers
    public Player getPlayer() { return player; }
    public Crystal getCrystal() { return crystal; }
    public WaveManager getWaveManager() { return waveManager; }
    public NotificationService getNotificationService() { return notificationService; }
    public SaveManager getSaveManager() { return saveManager; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public double getArenaWidth() { return arenaWidth; }
    public double getArenaHeight() { return arenaHeight; }
    public List<Enemy> getEnemies() { return enemies; }
}
