# Elemental Rift - Screenshot Submission Guide

This guide lists the exact screenshots you should capture from the running application for your project report and grading submission.

---

## Required Screenshots Checklist

### Screenshot 1: Main Menu Portal
- **Where**: Launch the game via `run.bat` or `mvn javafx:run`.
- **What to show**:
  - The Main Menu screen featuring the glowing cyan title banner **"ELEMENTAL RIFT"**.
  - The elemental matchup doctrine: `Water > Fire > Air > Earth > Water (2x Damage!)`.
  - The polished dark fantasy buttons: *Enter the Rift*, *Arcane Emporium*, *Grimoire of Trials*, *Grimoire of Controls*, and *Abandon Rift*.
- **Caption**: *"Figure 1: Main Menu screen demonstrating custom dark-fantasy CSS styling and navigation options."*

---

### Screenshot 2: Active Combat Arena (Wave 1 & 2)
- **Where**: Click *Enter the Rift* from Main Menu.
- **What to show**:
  - The top HUD showing Wave number, Score, Gold, Crystal HP Bar (cyan), and Mage Mana Bar (purple).
  - The Elemental Mage in the center-bottom aiming at incoming Goblins and Golems.
  - Spells in flight (e.g., fiery blast or water trident spray).
  - Glowing projectile tails, particle bursts, and floating damage numbers indicating elemental multipliers (e.g., `110 (2x)` in gold text).
  - The bottom spell hotbar with keys `[1]`, `[2]`, `[3]`, `[4]` and cooldown progress.
- **Caption**: *"Figure 2: Real-time arena canvas loop rendered with 60 FPS AnimationTimer, particle systems, and floating damage text."*

---

### Screenshot 3: Titan Boss Encounter (Wave 3)
- **Where**: Advance to Wave 3.
- **What to show**:
  - The warning banner alert in top HUD: `Wave: 3 [BOSS!]`.
  - The towering Boss Titan entity entering from the top with an ominous elemental crown and dual health bar.
  - Active screen shake displacement on the canvas.
  - Combat alert in the log: `ALERT: Boss Titan detected in Wave 3!`.
- **Caption**: *"Figure 3: Boss Titan wave demonstrating subclass polymorphism, screen-shake camera feedback, and custom health bars."*

---

### Screenshot 4: Crystal Degradation & Dynamic Fractures
- **Where**: Allow enemies to strike the Nexus Crystal until health falls below 50%.
- **What to show**:
  - The sacred crystal displaying visible fracture cracks (Levels 1 to 3) and shifting from ethereal cyan to warning amber/crimson core glow.
  - The crystal HP bar reflecting depleted health.
- **Caption**: *"Figure 4: Nexus Crystal degradation showing visual fracture cracks as health decreases."*

---

### Screenshot 5: Arcane Emporium (Shop Screen)
- **Where**: Click the *Shop* button in the top HUD or Main Menu.
- **What to show**:
  - The `TabPane` showing the three tabs: *Elemental Spells*, *Crystal Bastion*, and *Mage Mastery*.
  - Under *Elemental Spells*: The 4 spell cards (Inferno Blast, Tidal Surge, Gale Lance, Boulder Quake) with current tier, damage, cooldown, mana cost, and upgrade buttons.
  - Gold counter in the header reflecting current player earnings.
- **Caption**: *"Figure 5: Arcane Emporium interface implementing JavaFX TabPane and the Upgradable interface."*

---

### Screenshot 6: Grimoire of Trials (Stats & Analytics Screen)
- **Where**: Navigate to *Stats & Records*.
- **What to show**:
  - **Left Pane**: `TableView` showing archived game records with columns for Mage Name, Score, Wave, Kills, Counter %, and Date.
  - **Filter ComboBox**: Showing active filter selection (`All Trials`, `High Scores`, etc.).
  - **Right Pane**: Click *Run Background Deep Analysis* and capture the `ProgressBar` while processing, along with the detailed elemental damage breakdown and Archmage Grade (`S`, `A`, `B`, `C`).
  - **Bottom-Right**: `ListView` displaying the real-time combat notifications history.
- **Caption**: *"Figure 6: Combat analytics screen showcasing JavaFX TableView, ComboBox filtering, and background multithreaded report calculation (Thread 2) with a live ProgressBar."*

---

### Screenshot 7: Pause Overlay & Controls Dialog
- **Where**: Press `P` during active combat or click *Grimoire of Controls* in Main Menu.
- **What to show**:
  - The dark frosted pause overlay: `RITUAL SUSPENDED (PAUSED)` with *Resume Battle*, *Restart Arena*, and *Main Menu* buttons.
  - Or the JavaFX `Alert` dialog detailing keyboard controls and counter lore.
- **Caption**: *"Figure 7: In-game pause menu overlay and control doctrine alert dialog."*

---

### Screenshot 8: Plain-Java Test Suite Execution
- **Where**: Run `test.bat` in a terminal or command prompt.
- **What to show**:
  - Terminal output displaying the 69 passed tests across Element Counters, Polymorphism, Exceptions, Cooldowns, Upgrades, Crystal Cracks, and Multithreading.
  - The final summary line: `ALL TESTS PASSED SUCCESSFULLY! (100% OK)`.
- **Caption**: *"Figure 8: Standalone plain-Java unit test runner verifying all OOP logic, exceptions, and thread safety without JUnit."*
