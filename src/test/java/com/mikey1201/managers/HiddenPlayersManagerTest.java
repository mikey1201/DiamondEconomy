package com.mikey1201.managers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import com.mikey1201.DiamondEconomy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistence tests for the Gson-backed HiddenPlayersManager (requires
 * MockBukkit for a JavaPlugin instance and data folder).
 */
class HiddenPlayersManagerTest {

    private DiamondEconomy plugin;

    @BeforeEach
    void setUp() {
        System.setProperty("bstats.relocatecheck", "false");
        MockBukkit.mock();
        plugin = MockBukkit.load(DiamondEconomy.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void togglePersistsAcrossManagerInstances() {
        UUID uuid = UUID.randomUUID();
        HiddenPlayersManager manager = new HiddenPlayersManager(plugin);

        assertFalse(manager.isHidden(uuid));
        assertTrue(manager.toggle(uuid), "first toggle hides");
        assertTrue(manager.isHidden(uuid));

        // A fresh manager (simulating a restart) must see the saved state
        assertTrue(new HiddenPlayersManager(plugin).isHidden(uuid));

        // Untoggling also persists
        assertFalse(new HiddenPlayersManager(plugin).toggle(uuid), "second toggle reveals");
        assertFalse(new HiddenPlayersManager(plugin).isHidden(uuid));
    }

    @Test
    void legacyFileFormatStillLoads() throws Exception {
        // The pre-Gson writer produced the same JSON array-of-strings format;
        // hand-write one to prove old installs keep working.
        UUID uuid = UUID.randomUUID();
        File file = new File(plugin.getDataFolder(), "hidden_players.json");
        Files.write(file.toPath(),
                ("[\"" + uuid + "\"]").getBytes(StandardCharsets.UTF_8));

        assertTrue(new HiddenPlayersManager(plugin).isHidden(uuid));
    }

    @Test
    void corruptFileIsPreservedAndDoesNotCrash() throws Exception {
        File file = new File(plugin.getDataFolder(), "hidden_players.json");
        Files.write(file.toPath(), "this is not json {{{".getBytes(StandardCharsets.UTF_8));

        HiddenPlayersManager manager = new HiddenPlayersManager(plugin);
        assertFalse(manager.isHidden(UUID.randomUUID()), "corrupt file must load as empty");

        File corrupt = new File(plugin.getDataFolder(), "hidden_players.json.corrupt");
        assertTrue(corrupt.exists(), "corrupt file must be preserved for inspection");
        assertEquals("this is not json {{{",
                Files.readString(corrupt.toPath(), StandardCharsets.UTF_8));

        // And the manager remains fully usable afterwards
        UUID uuid = UUID.randomUUID();
        assertTrue(manager.toggle(uuid));
        assertTrue(new HiddenPlayersManager(plugin).isHidden(uuid));
    }

    @Test
    void emptyFileLoadsAsNoHiddenPlayers() throws Exception {
        File file = new File(plugin.getDataFolder(), "hidden_players.json");
        Files.write(file.toPath(), new byte[0]);

        assertFalse(new HiddenPlayersManager(plugin).isHidden(UUID.randomUUID()));
    }
}