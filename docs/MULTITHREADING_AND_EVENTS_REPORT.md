# Elemental Rift - Multithreading & JavaFX Event Handling Report

## 1. Multithreading Architecture Overview

In graphical real-time applications such as **Elemental Rift**, running heavy computations or long-running tasks on the JavaFX Application Thread causes the Graphical User Interface (GUI) to drop frames, stutter, or become completely unresponsive ("frozen").

To maintain a fluid 60 frames-per-second (FPS) simulation alongside responsive controls and heavy analytical processing, the application decouples execution across multiple independent threads:

```
+--------------------------------------------------------------------------+
|                        JavaFX Application Thread                         |
|  - Renders Canvas via AnimationTimer (60 FPS)                            |
|  - Dispatches Mouse Events (aiming, clicking) & Keyboard Events (1-4, P) |
|  - Updates HUD ProgressBars, Labels, and TableView elements              |
+--------------------------------------------------------------------------+
         ^                                           ^
         | Platform.runLater()                       | Platform.runLater()
         |                                           |
+-------------------------------+       +----------------------------------+
|  Thread 1: ScheduledExecutor  |       |   Thread 2: ReportGenerator      |
|  - 500ms Cadence: Mana Boost  |       |   - Calculates elemental damage  |
|  - 200ms Cadence: Spawner/    |       |   - Computes counter accuracy    |
|    Wave Milestone Monitor     |       |   - Streams progress 0% -> 100%  |
+-------------------------------+       +----------------------------------+
```

---

## 2. Independent Threading Tasks

### 2.1 Thread 1: Mana Regeneration & Wave Spawner Monitor
- **Implementation**: Utilizes a `ScheduledExecutorService` configured with daemon threads (`Executors.newScheduledThreadPool(2)`).
- **Responsibilities**:
  1. **Passive Mana Restoration**: Periodically restores player mana at 500ms intervals regardless of frame rate fluctuations.
  2. **Wave Spawner Monitor**: Polls wave progression at 200ms intervals. When all enemies in a wave are defeated and the spawn queue is exhausted, it grants bonus gold/score, triggers the next wave countdown, and notifies the UI via `Platform.runLater()`.
- **Why Multithreading is Useful Here**: Decoupling mana restoration and wave cadence from the frame rendering loop ensures that even if rendering undergoes a transient slowdown, game rules and timers tick accurately and steadily in the background.

---

### 2.2 Thread 2: Asynchronous Analytics & Report Generator
- **Implementation**: Implements `Runnable` through the `ReportGenerator` class, launched on a separate background worker thread.
- **Responsibilities**:
  - Traverses combat logs and metrics dictionaries to compute:
    - Cumulative damage dealt per element (Fire, Water, Air, Earth).
    - Enemy kill breakdown across distinct species (`Goblin`, `Golem`, `Wraith`, `Boss`).
    - Elemental counter accuracy (`successfulCounters / totalHits * 100.0`).
    - Assigns an Archmage Performance Grade (`S`, `A`, `B`, or `C`).
  - Streams progress updates (0.2 -> 0.5 -> 0.8 -> 1.0) so the JavaFX `ProgressBar` animates smoothly.
- **Why Multithreading is Useful Here**: Aggregate analytics and data-mining operations on extensive historical combat records can take several hundred milliseconds to execute. If run synchronously on the UI thread, the application would stutter noticeably. Offloading this workload keeps the UI completely fluid.

---

## 3. Thread-Safe Concurrency & Shared Data Protection

When multiple threads interact with shared game state, race conditions, memory inconsistency, and `ConcurrentModificationException` can occur. The application protects shared data using industry best practices:

1. **Concurrent Collections**:
   - `CopyOnWriteArrayList`: Used for active entity lists (`enemies`, `projectiles`, `particles`, `damageTexts`) and event listeners (`listeners` in `NotificationService`). This permits the rendering thread to iterate through the collections while background spawner threads add or remove entities without throwing `ConcurrentModificationException`.
   - `ConcurrentHashMap`: Used for real-time metric tracking (`damageDealtByElement`, `killsByEnemyType`). Permits atomic reads and writes (`merge()`, `put()`) across thread boundaries.
2. **Atomic Primitives**:
   - `AtomicInteger`: Used for `successfulCounterHits` and `totalHitsScored`. Guarantees atomic increments without requiring thread blocking.
3. **Synchronized Critical Sections**:
   - Methods that alter state across multiple related fields (`Player.spendMana()`, `Player.restoreMana()`, `Player.spendGold()`, `Crystal.takeDamage()`, `WaveManager.startNextWave()`) are declared with the `synchronized` keyword, guaranteeing atomic operations.
4. **`Platform.runLater()`**:
   - JavaFX is not thread-safe: any modification to scene graph nodes, labels, or progress bars from a worker thread must be dispatched via `Platform.runLater(Runnable)`. All notifications and progress events in `NotificationService` and `ReportGenerator` strictly verify `Platform.isFxApplicationThread()` and dispatch onto the UI thread.

---

## 4. JavaFX Event Handling Architecture

In strict adherence to the application architecture rules, **no game logic is embedded inside JavaFX event handlers**. Handlers serve solely as thin dispatchers that translate user intent into `GameEngine` method invocations.

| UI Component | Event Type | Target Method in `GameEngine` / `Player` | Purpose |
| :--- | :--- | :--- | :--- |
| **Canvas** | `setOnMouseMoved` | `player.setAimAngle(angle)` | Updates mage orientation toward cursor |
| **Canvas** | `setOnMouseClicked` | `gameEngine.castSelectedSpell(x, y)` | Fires selected spell at cursor coordinate |
| **Canvas** | `setOnMouseDragged` | `gameEngine.castSelectedSpell(x, y)` | Continuous aiming and casting |
| **GameScreen** | `setOnKeyPressed` (`DIGIT1`-`4`) | `player.selectSpell(index)` | Switches active spell to Fire/Water/Air/Earth |
| **GameScreen** | `setOnKeyPressed` (`KeyCode.P`) | `gameEngine.setGameState(PAUSED)` | Toggles pause overlay and stops simulation |
| **MenuBar** | `setOnAction` (Menu Items) | `gameEngine.restartGame()` / `app.show...()` | Triggers arena restarts or screen transitions |
| **ShopScreen** | `setOnAction` (Upgrade Buttons) | `gameEngine.upgradeSpell(index)` | Spends gold and levels up spell stats |
| **StatsScreen** | `selectedItemProperty().addListener` | `selectedRecordDetailsLabel.setText(...)` | Inspects selected historical trial |
| **StatsScreen** | `setOnAction` (ComboBox) | `applyFilter(filter)` | Filters high score TableView records |
| **StatsScreen** | `setOnAction` (Run Analytics Button)| `gameEngine.generateAnalyticsReport(...)` | Dispatches background thread (Thread 2) |
