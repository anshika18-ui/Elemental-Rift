# Elemental Rift - Object-Oriented Design Document

## 1. Executive Summary
**Elemental Rift** is a university-level real-time elemental defense game developed in Java 17+ and JavaFX. The player assumes the role of an Elemental Mage defending a sacred Nexus Crystal against escalating waves of elemental invaders. The game is architected around strict Object-Oriented Programming (OOP) paradigms: comprehensive encapsulation, rich inheritance hierarchies, interface-driven polymorphic behaviors, custom exception propagation, multi-tiered thread concurrency, and clean separation between Model, Service, and UI layers.

---

## 2. Core OOP Concepts Demonstrated

### 2.1 Classes & Objects
The application features over 18 distinct classes and enums organized across dedicated packages:
- **`model/`**: `Entity`, `Player`, `Enemy`, `Goblin`, `Golem`, `Wraith`, `Boss`, `Spell`, `FireSpell`, `WaterSpell`, `AirSpell`, `EarthSpell`, `Projectile`, `Particle`, `DamageText`, `Crystal`, `Element`, `GameState`, `GameRecord`.
- **`interfaces/`**: `Drawable`, `Castable`, `Upgradable`, `Notifiable`.
- **`exceptions/`**: `NotEnoughManaException`, `SpellOnCooldownException`, `InvalidSaveFileException`.
- **`service/`**: `GameEngine`, `WaveManager`, `ReportGenerator`, `NotificationService`, `SaveManager`.
- **`ui/`**: `MainMenu`, `GameScreen`, `ShopScreen`, `StatsScreen`.

Core game state is maintained exclusively in heap-allocated objects; no static state is used for mutable combat data.

---

### 2.2 Encapsulation
Encapsulation is enforced throughout all domain models:
- All instance variables (`x`, `y`, `health`, `maxHealth`, `mana`, `cooldownTimer`, `gold`, `score`) are marked `private`.
- Public access is controlled via explicit getters and mutators with strict boundary validation:
  - `Entity.takeDamage(damage)`: Ensures incoming damage is non-negative, clamps damage to current health, triggers `onDeath()` when health reaches zero, and updates `alive` flag.
  - `Entity.heal(amount)`: Restores health without exceeding `maxHealth`.
  - `Player.spendMana(amount)`: Validates mana reserves and throws `NotEnoughManaException` if insufficient.
  - `Crystal.getCrackLevel()`: Calculates structural degradation into 5 discrete tiers (0 to 4) depending on the remaining HP ratio.

---

### 2.3 Inheritance Hierarchy

#### A. The `Entity` Tree
```
              [Entity (Abstract)]
             /                  \
      [Player]                  [Enemy (Abstract)]
                               /    |     \       \
                       [Goblin] [Golem] [Wraith] [Boss]
```
- **`Entity` (Abstract)**: Implements `Drawable`. Defines common 2D coordinate spaces, dimensions, bounding circles, hit-points, velocities, and abstract `update(double delta)`.
- **`Player`**: Adds mana pools, mana regeneration rates, gold, score, an active spellbook (`List<Spell>`), aim angles, and specialized rendering.
- **`Enemy` (Abstract)**: Encapsulates attack cooldowns, attack powers, gold/score rewards, and provides abstract methods `move(double delta, Crystal target)` and `attack(Crystal crystal)`.
- **`Goblin`**: Fast, lightweight skirmisher implementing a zig-zag darting kinematic trajectory.
- **`Golem`**: Armored colossus possessing heavy health, slower movement, and crushing crystal strikes.
- **`Wraith`**: Ethereal specter weaving sinusoidally using trigonometric sweep paths.
- **`Boss`**: Massive elemental titan with an enrage mechanic (<35% HP), crown aura, screen-shake triggers, and boss-tier health scaling.

#### B. The `Spell` Tree
```
                    [Spell (Abstract)]
                (implements Castable, Upgradable)
                /         |         \         \
         [FireSpell] [WaterSpell] [AirSpell] [EarthSpell]
```
- **`Spell` (Abstract)**: Manages base damage, mana cost, cooldown timers, level tiers, and pre-cast checks.
- **`FireSpell` ("Inferno Blast")**: Blazing pyro-sphere that explodes into ember particles. Deals 2x damage against Air.
- **`WaterSpell` ("Tidal Surge")**: Trident 3-projectile hydro spray with wide coverage. Deals 2x damage against Fire.
- **`AirSpell` ("Gale Lance")**: Hyper-sonic piercing vortex slicing through up to 3 enemies. Deals 2x damage against Earth.
- **`EarthSpell` ("Boulder Quake")**: Colossal seismic boulder with high damage and shrapnel. Deals 2x damage against Water.

---

### 2.4 Polymorphism
Polymorphism is demonstrated in multiple fundamental game systems:
1. **Dynamic Method Dispatch in Collections**:
   - `List<Enemy>`: `GameEngine` iterates over an array list of `Enemy` references. When calling `enemy.move(delta, crystal)` and `enemy.draw(gc)`, the JVM resolves the concrete implementation at runtime (`Goblin`, `Golem`, `Wraith`, or `Boss`).
   - `List<Spell>`: `Player` stores an array list of `Spell` references. Invoking `spell.cast(...)` executes the subclass-specific projectile instantiation logic.
2. **Interface Polymorphism**:
   - `List<Drawable>`: The render loop iterates through heterogeneous visual objects (`Crystal`, `Player`, `Enemy`, `Projectile`, `Particle`, `DamageText`) via `Drawable` references, calling `.draw(gc)` without knowing their underlying concrete classes.
   - `Upgradable`: The `ShopScreen` interacts with spells exclusively through the `Upgradable` interface (`upgrade()`, `getUpgradeCost()`, `getLevel()`).

---

### 2.5 Interfaces (4 Complete Interfaces)
1. **`Drawable`**: Defines `void draw(GraphicsContext gc)`. Unifies all renderable game elements onto the JavaFX Canvas.
2. **`Castable`**: Defines `void cast(...)`, `double getManaCost()`, and `boolean isReady()`. Decouples spell invocation from concrete projectile types.
3. **`Upgradable`**: Defines `void upgrade()`, `int getUpgradeCost()`, `int getLevel()`, and `String getUpgradeDescription()`.
4. **`Notifiable`**: Defines `void onNotification(String message, String type)`. Allows decoupled event broadcast from services to UI displays.

---

### 2.6 Method Design & Single Responsibility
Distinct responsibilities are isolated into granular methods:
- `spawnWave()`: Manages wave population, boss wave flags, and spawn queue scaling.
- `calculateDamage()` & `applyElementBonus()`: Evaluates attacker element vs defender element and returns 2.0x, 0.5x, or 1.0x.
- `checkCollision()`: Executes circular radius collision tests between projectiles and enemies.
- `upgradeSpell()`: Validates player gold, deducts currency, and calls `Upgradable.upgrade()`.

---

### 2.7 Custom Exception Handling
Three checked exceptions are implemented and utilized:
1. **`NotEnoughManaException`**: Thrown when a player attempts to cast a spell without sufficient mana. Carries required mana and current mana fields.
2. **`SpellOnCooldownException`**: Thrown when a player attempts to cast before the spell cooldown elapses. Carries remaining cooldown seconds.
3. **`InvalidSaveFileException`**: Thrown when reading a corrupted or malformed CSV record file. Tracks the filename and line number of the corruption.

All exceptions are caught gracefully: `GameEngine` dispatches in-game combat notifications on mana/cooldown errors, and `StatsScreen` displays a JavaFX `Alert` dialog on corrupted save files, preventing application crashes.

---

## 3. Elemental Counters

```
  +--------+   (Water douses Fire)   +--------+
  | WATER  | ----------------------> |  FIRE  |
  +--------+                         +--------+
      ^                                  |
      | (Earth absorbs Water)            | (Fire consumes Air)
      |                                  v
  +--------+   (Air erodes Earth)    +--------+
  | EARTH  | <---------------------- |  AIR   |
  +--------+                         +--------+
```

- **Advantage (2.0x Damage)**: Water > Fire, Fire > Air, Air > Earth, Earth > Water.
- **Disadvantage (0.5x Damage)**: Fire vs Water, Air vs Fire, Earth vs Air, Water vs Earth.
- **Neutral (1.0x Damage)**: Same elements or Neutral (NONE).
