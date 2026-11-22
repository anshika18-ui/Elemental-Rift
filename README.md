# Elemental Rift 🔮⚔️
> **A Real-Time Elemental Arena Defense Game in Pure Java & JavaFX**
> University Object-Oriented Programming (OOP) Showcase Project

---

## 📖 Game Overview
In **Elemental Rift**, you play as an elemental archmage defending the sacred **Nexus Crystal** against relentless waves of elemental creatures. You command the four classical elements: **Fire**, **Water**, **Air**, and **Earth**. 

Victory requires tactical mastery over the **Elemental Counter Cycle**:
- 🌊 **Water** douses 🔥 **Fire** (deal **2.0x** double damage!)
- 🔥 **Fire** consumes 💨 **Air** (deal **2.0x** double damage!)
- 💨 **Air** erodes 🪨 **Earth** (deal **2.0x** double damage!)
- 🪨 **Earth** absorbs 🌊 **Water** (deal **2.0x** double damage!)
- *Wrong matchup deals 0.5x damage; matching or neutral matchups deal 1.0x damage.*

Enemies advance toward the crystal from the upper rifts. If the crystal's hit points reach zero, the defenses collapse and the game ends!

---

## 🎮 Game Controls
- **Aim**: Move mouse across the arena. The mage rotates and aims at your cursor.
- **Cast Spell**: **Left-Click** or drag anywhere on the arena canvas.
- **Spell Selection**:
  - `[1]` **Inferno Blast** (Fire) - High single-target destructive pyro-sphere. Counters Air.
  - `[2]` **Tidal Surge** (Water) - Trident spray of 3 hydro-projectiles. Counters Fire.
  - `[3]` **Gale Lance** (Air) - Ultra-fast piercing vortex (pierces up to 3 enemies). Counters Earth.
  - `[4]` **Boulder Quake** (Earth) - Massive seismic rock missile with heavy shrapnel. Counters Water.
  - *Or click on the bottom spell hotbar icons directly.*
- **Pause / Resume**: Press `P` key or use the top menu bar.
- **Navigation**: Access the **Arcane Emporium (Shop)** or **Grimoire (Stats)** anytime from the top HUD.

---

## 🏗️ Architecture & OOP Implementation

### Mandatory OOP Requirements Checklist

| Requirement | Implementation Detail | Location |
| :--- | :--- | :--- |
| **1. Classes & Objects** | 18+ distinct domain classes & enums with single responsibilities | `com.elementalrift.model.*` |
| **2. Encapsulation** | All fields `private`; strict mutators with validation (`takeDamage`, `spendMana`) | `Entity`, `Player`, `Crystal`, etc. |
| **3. Constructors** | Default and parameterized constructors used across models | `Entity`, `Player`, `Enemy`, `Crystal` |
| **4. Inheritance** | `Entity -> Player, Enemy`; `Enemy -> Goblin, Golem, Wraith, Boss`; `Spell -> FireSpell, WaterSpell, AirSpell, EarthSpell` | `com.elementalrift.model` |
| **5. Polymorphism** | Polymorphic lists `List<Enemy>`, `List<Spell>`, `List<Drawable>` with dynamic method dispatch (`move()`, `attack()`, `cast()`, `draw()`) | `GameEngine`, `Player`, `ShopScreen` |
| **6. Arrays/ArrayList** | Managed lists: `List<Enemy>`, `List<Spell>`, `List<Projectile>`, `List<Particle>`, `List<DamageText>` | `GameEngine`, `Player` |
| **7. Method Design** | Granular methods: `spawnWave()`, `calculateDamage()`, `checkCollisions()`, `upgradeSpell()` | `GameEngine`, `WaveManager`, `Element` |
| **8. Abstract Classes** | `Entity` (abstract base for all combatants) and `Spell` (abstract base for all spells) | `Entity.java`, `Spell.java` |
| **9. Interfaces (4 total)** | `Drawable` (rendering), `Castable` (spell execution), `Upgradable` (shop tiers), `Notifiable` (combat alerts) | `com.elementalrift.interfaces.*` |
| **10. Exception Handling** | Custom checked exceptions: `NotEnoughManaException`, `SpellOnCooldownException`, `InvalidSaveFileException`. Handled gracefully with UI alerts without crashing. | `com.elementalrift.exceptions.*` |
| **11. JavaFX Event Handling** | Mouse clicks, mouse dragging, keys 1-4, key P, menu actions, ComboBox selection, and TableView row selection. | `GameScreen`, `StatsScreen`, `MainMenu` |
| **12. Multithreading** | **Thread 1**: `ScheduledExecutorService` (mana boost & wave spawner monitor). **Thread 2**: `ReportGenerator` (background analytics calculation with `ProgressBar` and `Platform.runLater()`). Thread-safe collections (`CopyOnWriteArrayList`, `ConcurrentHashMap`, `AtomicInteger`, `synchronized`). | `GameEngine.java`, `ReportGenerator.java` |
| **13. JavaFX GUI (4 Screens)** | **Main Menu** -> **Game Screen** -> **Shop Screen** -> **Stats Screen**. Features `Canvas`, `ProgressBar`, `TableView`, `ListView`, `TabPane`, `ComboBox`, `MenuBar`, `Alert`. | `com.elementalrift.ui.*` |

---

## 📂 Project Directory Structure

```
element war games/
├── pom.xml                               # Maven project configuration (JavaFX plugin)
├── build.bat                             # 1-Click build script
├── run.bat                               # 1-Click game launcher
├── test.bat                              # 1-Click plain Java test suite runner
├── lib/                                  # Self-contained JavaFX library JARs
│   ├── javafx-base.jar
│   ├── javafx-graphics.jar
│   └── javafx-controls.jar
├── docs/                                 # University submission documentation
│   ├── UML_DIAGRAM.md                    # Complete Mermaid class diagrams
│   ├── DESIGN_DOCUMENT.md                # Comprehensive OOP design document
│   ├── MULTITHREADING_AND_EVENTS_REPORT.md # Concurrency & event handling report
│   └── SCREENSHOT_SUBMISSION_GUIDE.md    # Guide to capturing report screenshots
└── src/
    ├── main/
    │   ├── java/com/elementalrift/
    │   │   ├── MainApp.java              # JavaFX Application entry point
    │   │   ├── exceptions/               # Custom checked exceptions
    │   │   │   ├── NotEnoughManaException.java
    │   │   │   ├── SpellOnCooldownException.java
    │   │   │   └── InvalidSaveFileException.java
    │   │   ├── interfaces/               # 4 Core interfaces
    │   │   │   ├── Drawable.java
    │   │   │   ├── Castable.java
    │   │   │   ├── Upgradable.java
    │   │   │   └── Notifiable.java
    │   │   ├── model/                    # Domain models & entities
    │   │   │   ├── Element.java          # Elemental enum & counter logic
    │   │   │   ├── Entity.java           # Abstract entity base
    │   │   │   ├── Player.java           # Elemental mage
    │   │   │   ├── Crystal.java          # Nexus crystal with crack tiers
    │   │   │   ├── Enemy.java            # Abstract enemy base
    │   │   │   ├── Goblin.java           # Nimble runner subclass
    │   │   │   ├── Golem.java            # Armored tank subclass
    │   │   │   ├── Wraith.java           # Ethereal sweeping specter subclass
    │   │   │   ├── Boss.java             # Colossal titan boss subclass
    │   │   │   ├── Spell.java            # Abstract spell base
    │   │   │   ├── FireSpell.java        # Inferno Blast subclass
    │   │   │   ├── WaterSpell.java       # Tidal Surge subclass
    │   │   │   ├── AirSpell.java         # Gale Lance subclass
    │   │   │   ├── EarthSpell.java       # Boulder Quake subclass
    │   │   │   ├── Projectile.java       # Projectile kinematics & collision
    │   │   │   ├── Particle.java         # Visual particle system
    │   │   │   ├── DamageText.java       # Floating combat damage text
    │   │   │   ├── GameState.java        # Game lifecycle states
    │   │   │   └── GameRecord.java       # TableView & CSV persistence POJO
    │   │   ├── service/                  # Game engine & worker services
    │   │   │   ├── GameEngine.java       # Master coordinator & 60 FPS loop
    │   │   │   ├── WaveManager.java      # Wave generation & boss encounters
    │   │   │   ├── ReportGenerator.java  # Multithreaded background analytics (Thread 2)
    │   │   │   ├── NotificationService.java # Thread-safe UI alert dispatcher
    │   │   │   └── SaveManager.java      # CSV persistence & validation
    │   │   └── ui/                       # JavaFX GUI screens
    │   │       ├── MainMenu.java         # Screen 1: Portal & main options
    │   │       ├── GameScreen.java       # Screen 2: Canvas arena & HUD
    │   │       ├── ShopScreen.java       # Screen 3: Arcane emporium & TabPane
    │   │       └── StatsScreen.java      # Screen 4: TableView, ComboBox & ProgressBar
    │   └── resources/
    │       └── style.css                 # Dark fantasy CSS theme
    └── test/
        └── java/com/elementalrift/test/
            └── TestRunner.java           # Standalone plain Java test runner
```

---

## 🚀 How to Compile & Run

### Method 1: One-Click Batch Scripts (Recommended for Windows)
No Maven installation or configuration required! The project includes self-contained JavaFX libraries in `lib/`:
```bat
# 1. To run the automated Plain-Java unit test suite (69 tests):
test.bat

# 2. To compile and launch the game:
run.bat
```

### Method 2: Maven CLI
If you have Maven installed on your machine:
```bash
# Compile and run via the JavaFX Maven plugin:
mvn javafx:run
```

### Method 3: Manual Terminal Command
```bash
# 1. Compile all Java sources
javac -d target/classes -cp "lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar" (Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse).FullName

# 2. Copy CSS styling resources
xcopy /s /y /q src\main\resources\* target\classes\

# 3. Launch Application
java -cp "target/classes;lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar" com.elementalrift.MainApp

# 4. Or Run Plain-Java Test Suite
java -cp "target/classes;lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar" com.elementalrift.test.TestRunner
```

---

## 🧪 Automated Plain-Java Unit Test Suite

The test suite does **not** rely on JUnit; it executes via a pure Java `TestRunner.main()` verifying:
- ✅ **11 Elemental Counter Tests**: Confirms 2.0x advantage, 0.5x disadvantage, and 1.0x neutral damage multipliers across all pairs.
- ✅ **13 Polymorphism Tests**: Verifies heterogeneous `List<Spell>` and `List<Enemy>` dynamic method dispatch.
- ✅ **Exception Handling Tests**:
  - `NotEnoughManaException`: Thrown and caught when mana is insufficient.
  - `SpellOnCooldownException`: Thrown and caught on rapid consecutive casts.
  - `InvalidSaveFileException`: Thrown and caught when parsing corrupted CSV records.
- ✅ **Upgrade Scaling Tests**: Validates `Upgradable` damage scaling and cost multipliers.
- ✅ **Entity & Crystal Degradation**: Validates HP boundaries, death triggers, and the 5-tier crystal fracture system.
- ✅ **Thread Concurrency**: Validates background execution of `ReportGenerator` without deadlock or race conditions.

**Result**: 69 / 69 Tests Passing (100% OK).

---

## 📜 Documentation Links
- [UML Class Diagram](docs/UML_DIAGRAM.md)
- [OOP Design Document](docs/DESIGN_DOCUMENT.md)
- [Multithreading & Event Handling Report](docs/MULTITHREADING_AND_EVENTS_REPORT.md)
- [Screenshot Submission Guide](docs/SCREENSHOT_SUBMISSION_GUIDE.md)
