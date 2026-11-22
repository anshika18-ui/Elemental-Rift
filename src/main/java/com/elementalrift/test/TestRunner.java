package com.elementalrift.test;

import com.elementalrift.exceptions.InvalidSaveFileException;
import com.elementalrift.exceptions.NotEnoughManaException;
import com.elementalrift.exceptions.SpellOnCooldownException;
import com.elementalrift.interfaces.Castable;
import com.elementalrift.interfaces.Drawable;
import com.elementalrift.interfaces.Upgradable;
import com.elementalrift.model.*;
import com.elementalrift.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standalone Plain-Java Test Runner for Elemental Rift.
 * [OOP Testing Requirement: Test with plain Java (simple test runner with main()), not JUnit]
 * Verifies all game logic, element matchups, exception handling, polymorphism,
 * multithreading, and file persistence.
 */
public class TestRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("     ELEMENTAL RIFT - OOP UNIT TEST RUNNER       ");
        System.out.println("=================================================");

        testElementCounters();
        testPolymorphismSpellList();
        testPolymorphismEnemyList();
        testManaSpendingAndException();
        testSpellCooldownAndException();
        testSpellUpgrades();
        testEntityDamageAndDeath();
        testCrystalCrackLevels();
        testGameRecordSerialization();
        testWaveManagerAndBoss();
        testSaveManagerAndInvalidSaveException();
        testMultithreadedReportGenerator();

        System.out.println("=================================================");
        System.out.printf("TEST SUMMARY: %d Total | %d Passed | %d Failed\n", totalTests, passedTests, failedTests);
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100% OK)");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println(" [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println(" [FAIL] " + testName);
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double delta) {
        totalTests++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            System.out.printf(" [PASS] %s (Expected: %.2f, Got: %.2f)\n", testName, expected, actual);
        } else {
            failedTests++;
            System.err.printf(" [FAIL] %s (Expected: %.2f, Got: %.2f)\n", testName, expected, actual);
        }
    }

    private static void testElementCounters() {
        System.out.println("\n--- Testing Element Counters and Damage Multipliers ---");
        // Advantage matchups: 2.0x
        assertEquals("Water counters Fire (2.0x)", 2.0, Element.getDamageMultiplier(Element.WATER, Element.FIRE), 0.001);
        assertEquals("Fire counters Air (2.0x)", 2.0, Element.getDamageMultiplier(Element.FIRE, Element.AIR), 0.001);
        assertEquals("Air counters Earth (2.0x)", 2.0, Element.getDamageMultiplier(Element.AIR, Element.EARTH), 0.001);
        assertEquals("Earth counters Water (2.0x)", 2.0, Element.getDamageMultiplier(Element.EARTH, Element.WATER), 0.001);

        // Disadvantage matchups: 0.5x
        assertEquals("Fire vs Water is weak (0.5x)", 0.5, Element.getDamageMultiplier(Element.FIRE, Element.WATER), 0.001);
        assertEquals("Air vs Fire is weak (0.5x)", 0.5, Element.getDamageMultiplier(Element.AIR, Element.FIRE), 0.001);
        assertEquals("Earth vs Air is weak (0.5x)", 0.5, Element.getDamageMultiplier(Element.EARTH, Element.AIR), 0.001);
        assertEquals("Water vs Earth is weak (0.5x)", 0.5, Element.getDamageMultiplier(Element.WATER, Element.EARTH), 0.001);

        // Neutral matchups: 1.0x
        assertEquals("Fire vs Fire is neutral (1.0x)", 1.0, Element.getDamageMultiplier(Element.FIRE, Element.FIRE), 0.001);
        assertEquals("Fire vs Earth is neutral (1.0x)", 1.0, Element.getDamageMultiplier(Element.FIRE, Element.EARTH), 0.001);
        assertEquals("Neutral element against Fire is 1.0x", 1.0, Element.getDamageMultiplier(Element.NONE, Element.FIRE), 0.001);
    }

    private static void testPolymorphismSpellList() {
        System.out.println("\n--- Testing Polymorphism with ArrayList<Spell> and Castable ---");
        List<Spell> spellBook = new ArrayList<>();
        spellBook.add(new FireSpell());
        spellBook.add(new WaterSpell());
        spellBook.add(new AirSpell());
        spellBook.add(new EarthSpell());

        assertTrue("Spell list contains 4 distinct subclasses", spellBook.size() == 4);

        for (Spell s : spellBook) {
            Castable castableRef = s;
            assertTrue("Spell " + s.getName() + " implements Castable", castableRef != null);
            assertTrue("Spell " + s.getName() + " has valid mana cost", s.getManaCost() > 0);
            assertTrue("Spell " + s.getName() + " has custom effect description", s.getEffectDescription().length() > 5);
        }
    }

    private static void testPolymorphismEnemyList() {
        System.out.println("\n--- Testing Polymorphism with ArrayList<Enemy> and Entity Hierarchy ---");
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(100, 100, Element.EARTH));
        enemies.add(new Golem(200, 200, Element.FIRE));
        enemies.add(new Wraith(300, 300, Element.AIR));
        enemies.add(new Boss(400, 400, Element.WATER, 3));

        assertTrue("Enemy list contains 4 polymorphic subclasses", enemies.size() == 4);

        Crystal crystal = new Crystal(400, 500, 30, 500);
        double initialCrystalHealth = crystal.getHealth();

        for (Enemy e : enemies) {
            Entity entityRef = e;
            Drawable drawableRef = e;
            assertTrue("Enemy " + e.getClass().getSimpleName() + " is an Entity", entityRef != null);
            assertTrue("Enemy " + e.getClass().getSimpleName() + " is a Drawable", drawableRef != null);
            e.attack(crystal);
        }

        assertTrue("All enemies dealt damage to crystal via polymorphic attack()", crystal.getHealth() < initialCrystalHealth);
    }

    private static void testManaSpendingAndException() {
        System.out.println("\n--- Testing Mana Spending & NotEnoughManaException ---");
        Player player = new Player(400, 500);
        player.setMana(30.0);

        List<Projectile> projectiles = new ArrayList<>();
        EarthSpell earthSpell = new EarthSpell();

        boolean exceptionCaught = false;
        try {
            earthSpell.cast(player, 400, 200, projectiles);
        } catch (NotEnoughManaException e) {
            exceptionCaught = true;
            assertEquals("Required mana in exception matches", 32.0, e.getRequiredMana(), 0.001);
            assertEquals("Current mana in exception matches", 30.0, e.getCurrentMana(), 0.001);
        } catch (SpellOnCooldownException ignored) {}

        assertTrue("NotEnoughManaException was thrown when casting without sufficient mana", exceptionCaught);
    }

    private static void testSpellCooldownAndException() {
        System.out.println("\n--- Testing Spell Cooldown & SpellOnCooldownException ---");
        Player player = new Player(400, 500);
        player.setMana(150.0);

        List<Projectile> projectiles = new ArrayList<>();
        FireSpell fireSpell = new FireSpell();

        try {
            fireSpell.cast(player, 400, 200, projectiles);
            assertTrue("First cast succeeded and created projectile", projectiles.size() == 1);
        } catch (Exception e) {
            assertTrue("First cast failed unexpectedly", false);
        }

        boolean cooldownCaught = false;
        try {
            fireSpell.cast(player, 400, 200, projectiles);
        } catch (SpellOnCooldownException e) {
            cooldownCaught = true;
            assertTrue("Remaining cooldown reported > 0", e.getRemainingCooldownSeconds() > 0);
        } catch (NotEnoughManaException ignored) {}

        assertTrue("SpellOnCooldownException was thrown on rapid re-cast", cooldownCaught);
    }

    private static void testSpellUpgrades() {
        System.out.println("\n--- Testing Upgradable Interface & Stat Scaling ---");
        Upgradable spell = new FireSpell();
        int initialLevel = spell.getLevel();
        int cost = spell.getUpgradeCost();
        assertTrue("Initial level is 1", initialLevel == 1);
        assertTrue("Initial upgrade cost > 0", cost > 0);

        spell.upgrade();
        assertTrue("Level increased to 2 after upgrade()", spell.getLevel() == 2);
        assertTrue("Upgrade cost scaled up", spell.getUpgradeCost() > cost);
    }

    private static void testEntityDamageAndDeath() {
        System.out.println("\n--- Testing Entity Encapsulation, Damage & Death ---");
        Goblin goblin = new Goblin(100, 100, Element.EARTH);
        assertTrue("Goblin starts alive", goblin.isAlive());

        double dealt = goblin.takeElementalDamage(30.0, Element.AIR);
        assertEquals("Air damage against Earth Goblin is doubled (60)", 60.0, dealt, 0.001);
        assertTrue("Goblin died from lethal elemental counter", !goblin.isAlive());
        assertEquals("Health capped at 0 on death", 0.0, goblin.getHealth(), 0.001);
    }

    private static void testCrystalCrackLevels() {
        System.out.println("\n--- Testing Crystal Health Degradation & Crack Levels ---");
        Crystal crystal = new Crystal(400, 500, 30, 1000);
        assertEquals("Full health crack level is 0", 0, crystal.getCrackLevel(), 0);

        crystal.takeDamage(300);
        assertEquals("70% health crack level is 1", 1, crystal.getCrackLevel(), 0);

        crystal.takeDamage(300);
        assertEquals("40% health crack level is 2", 2, crystal.getCrackLevel(), 0);

        crystal.takeDamage(300);
        assertEquals("10% health crack level is 3", 3, crystal.getCrackLevel(), 0);

        crystal.takeDamage(200);
        assertTrue("Crystal is destroyed at 0 HP", crystal.isDestroyed());
        assertEquals("0% health crack level is 4", 4, crystal.getCrackLevel(), 0);
    }

    private static void testGameRecordSerialization() {
        System.out.println("\n--- Testing GameRecord CSV Serialization ---");
        GameRecord record = new GameRecord("ArchmageAlex", 12500, 8, 45, 87.5, "2026-11-22 23:00");
        String csv = record.toCsvLine();
        GameRecord parsed = GameRecord.fromCsvLine(csv);

        assertTrue("Player name preserved", parsed.getPlayerName().equals("ArchmageAlex"));
        assertTrue("Score preserved", parsed.getScore() == 12500);
        assertTrue("Wave reached preserved", parsed.getWaveReached() == 8);
        assertTrue("Enemies killed preserved", parsed.getEnemiesKilled() == 45);
        assertEquals("Elemental accuracy preserved", 87.5, parsed.getElementalAccuracy(), 0.01);
    }

    private static void testWaveManagerAndBoss() {
        System.out.println("\n--- Testing WaveManager & Boss Spawning Logic ---");
        WaveManager wm = new WaveManager();
        wm.startNextWave(); // Wave 1
        assertTrue("Wave 1 is not boss wave", !wm.isCurrentWaveBossWave());
        assertTrue("Wave 1 has queued enemies", wm.getRemainingInQueue() > 0);

        wm.startNextWave(); // Wave 2
        assertTrue("Wave 2 is not boss wave", !wm.isCurrentWaveBossWave());

        wm.startNextWave(); // Wave 3
        assertTrue("Wave 3 is detected as Boss wave", wm.isCurrentWaveBossWave());
        assertEquals("Wave 3 number is 3", 3, wm.getCurrentWave(), 0);
    }

    private static void testSaveManagerAndInvalidSaveException() {
        System.out.println("\n--- Testing SaveManager & InvalidSaveFileException ---");
        try {
            Path tempValid = Files.createTempFile("test_valid", ".csv");
            SaveManager smValid = new SaveManager(tempValid);

            List<GameRecord> list = new ArrayList<>();
            list.add(new GameRecord("Hero1", 5000, 4, 20, 80.0, "2026-11-22"));
            smValid.saveRecords(list);

            List<GameRecord> loaded = smValid.loadRecords();
            assertTrue("SaveManager successfully loaded 1 record", loaded.size() == 1);
            assertTrue("Loaded record player name matches", loaded.get(0).getPlayerName().equals("Hero1"));
            Files.deleteIfExists(tempValid);

            // Now test corrupted file throwing InvalidSaveFileException
            Path tempCorrupt = Files.createTempFile("test_corrupt", ".csv");
            Files.writeString(tempCorrupt, "CorruptDataWithoutProperColumns\nNotAValidNumber,XYZ,BadData\n");

            SaveManager smCorrupt = new SaveManager(tempCorrupt);
            boolean corruptCaught = false;
            try {
                smCorrupt.loadRecords();
            } catch (InvalidSaveFileException e) {
                corruptCaught = true;
                System.out.println(" Correctly caught expected InvalidSaveFileException: " + e.getMessage());
                assertTrue("Line number reported", e.getLineNumber() > 0);
            }
            assertTrue("InvalidSaveFileException thrown on corrupted CSV file", corruptCaught);
            Files.deleteIfExists(tempCorrupt);

        } catch (IOException | InvalidSaveFileException e) {
            assertTrue("File IO or parsing failed unexpectedly: " + e.getMessage(), false);
        }
    }

    private static void testMultithreadedReportGenerator() {
        System.out.println("\n--- Testing Multithreaded ReportGenerator (Thread 2) ---");
        Map<Element, Double> damageMap = new HashMap<>();
        damageMap.put(Element.FIRE, 500.0);
        damageMap.put(Element.WATER, 300.0);

        Map<String, Integer> killsMap = new HashMap<>();
        killsMap.put("Goblin", 15);
        killsMap.put("Golem", 5);

        ReportGenerator generator = new ReportGenerator(
                damageMap, killsMap, 18, 20,
                progress -> {},
                report -> {}
        );

        Thread thread = new Thread(generator, "Test-ReportGen-Thread");
        thread.start();

        try {
            thread.join(1500);
            assertTrue("ReportGenerator background thread completed without crashing", !thread.isAlive());
        } catch (InterruptedException e) {
            assertTrue("Thread was interrupted", false);
        }
    }
}
