package com.mikey1201;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mikey1201.managers.DatabaseManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests of the plugin commands using an in-memory MockBukkit
 * server: real plugin lifecycle (onLoad/onEnable), real command dispatch,
 * real inventories and real SQLite storage. These replace the manual
 * "build, install jar, launch server, join client, type commands" ritual.
 */
class DiamondEconomyPluginTest {

    private ServerMock server;
    private DiamondEconomy plugin;
    private PlayerMock alice;
    private PlayerMock bob;

    @BeforeEach
    void setUp() {
        // bStats refuses to construct unless its classes were relocated by the
        // shade plugin (which only happens during packaging, not in tests).
        // This system property is bStats' documented opt-out for unit tests.
        System.setProperty("bstats.relocatecheck", "false");
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DiamondEconomy.class);
        // addPlayer fires PlayerJoinEvent, so accounts are created like on a real server
        alice = server.addPlayer("Alice");
        bob = server.addPlayer("Bob");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ---------- helpers ----------

    /** Drains all queued messages, converting to plain (color-stripped) text. */
    private List<String> messages(PlayerMock p) {
        List<String> out = new ArrayList<>();
        while (true) {
            Component c;
            try {
                c = p.nextComponentMessage();
            } catch (NoSuchElementException e) {
                break;
            }
            if (c == null) break;
            out.add(ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(c)));
        }
        return out;
    }

    private void assertReceived(List<String> msgs, String expected) {
        assertTrue(msgs.stream().anyMatch(m -> m.contains(expected)),
                "Expected a message containing \"" + expected + "\" but got: " + msgs);
    }

    private void assertNotReceived(List<String> msgs, String unexpected) {
        assertFalse(msgs.stream().anyMatch(m -> m.contains(unexpected)),
                "Did NOT expect a message containing \"" + unexpected + "\" but got: " + msgs);
    }

    private int count(PlayerMock p, Material m) {
        int n = 0;
        for (ItemStack it : p.getInventory().getStorageContents()) {
            if (it != null && it.getType() == m) n += it.getAmount();
        }
        return n;
    }

    private void give(PlayerMock p, Material m, int amount) {
        p.getInventory().addItem(new ItemStack(m, amount));
    }

    /** Sets a player's balance using the console admin command (op-level access). */
    private void setBalance(PlayerMock p, double amount) {
        assertTrue(server.dispatchCommand(server.getConsoleSender(), "eco set " + p.getName() + " " + amount),
                "eco set command should dispatch");
        messages(p); // clear noise
    }

    /** Runs a command as the player and returns all messages it produced. */
    private List<String> run(PlayerMock p, String command) {
        assertTrue(p.performCommand(command), "command should be handled: /" + command);
        return messages(p);
    }

    private DatabaseManager db() throws Exception {
        Field f = DiamondEconomy.class.getDeclaredField("databaseManager");
        f.setAccessible(true);
        return (DatabaseManager) f.get(plugin);
    }

    private void fillInventory(PlayerMock p) {
        for (int i = 0; i < 36; i++) {
            p.getInventory().setItem(i, new ItemStack(Material.COBBLESTONE, 64));
        }
    }

    // ---------- lifecycle ----------

    @Test
    void pluginEnablesAndRegistersAllCommands() {
        assertTrue(plugin.isEnabled(), "plugin should be enabled");
        for (String name : new String[]{"balance", "deposit", "withdraw", "pay", "baltop", "eco", "diamondeconomy"}) {
            PluginCommand cmd = plugin.getCommand(name);
            assertNotNull(cmd, "command /" + name + " should be registered");
            assertNotNull(cmd.getExecutor(), "command /" + name + " should have an executor");
        }
    }

    @Test
    void playerJoinCreatesAccountWithZeroBalance() {
        // Alice joined during setUp; /balance must show a real 0.00 account
        List<String> msgs = run(alice, "balance");
        assertReceived(msgs, "Your balance: 0.00");
    }

    // ---------- deposit ----------

    @Test
    void depositRemovesItemsAndCreditsBalance() {
        give(alice, Material.DIAMOND, 10);
        List<String> msgs = run(alice, "deposit 5");
        assertReceived(msgs, "You have deposited 5");
        assertReceived(msgs, "New balance: 5.00");
        assertEquals(5, count(alice, Material.DIAMOND), "5 of 10 diamonds should remain");
    }

    @Test
    void depositAllDepositsEverything() {
        give(alice, Material.DIAMOND, 7);
        List<String> msgs = run(alice, "deposit all");
        assertReceived(msgs, "You have deposited 7");
        assertReceived(msgs, "New balance: 7.00");
        assertEquals(0, count(alice, Material.DIAMOND));
    }

    @Test
    void depositInsufficientItemsChangesNothing() {
        give(alice, Material.DIAMOND, 3);
        List<String> msgs = run(alice, "deposit 5");
        assertReceived(msgs, "You only have 3");
        assertEquals(3, count(alice, Material.DIAMOND));
        assertReceived(run(alice, "balance"), "Your balance: 0.00");
    }

    @Test
    void depositRejectsInvalidAmounts() {
        give(alice, Material.DIAMOND, 10);
        for (String bad : new String[]{"NaN", "1e5", "2.5", "0", "-3", "99999999999999999999"}) {
            List<String> msgs = run(alice, "deposit " + bad);
            assertReceived(msgs, "Invalid number: " + bad);
        }
        assertEquals(10, count(alice, Material.DIAMOND), "no items may be taken for invalid input");
        assertReceived(run(alice, "balance"), "Your balance: 0.00");
    }

    @Test
    void depositRenamedItemsAreCountedAndRemoved() {
        // Regression test for the anvil-renamed item dupe: items must be
        // counted AND removed by type, ignoring metadata.
        give(alice, Material.DIAMOND, 3);
        ItemStack renamed = new ItemStack(Material.DIAMOND, 2);
        ItemMeta meta = renamed.getItemMeta();
        meta.setDisplayName("My Precious");
        renamed.setItemMeta(meta);
        alice.getInventory().addItem(renamed);

        List<String> msgs = run(alice, "deposit all");
        assertReceived(msgs, "You have deposited 5");
        assertEquals(0, count(alice, Material.DIAMOND), "renamed items must also be removed");
    }

    @Test
    void depositWithDatabaseDownDoesNotRemoveItems() throws Exception {
        // Regression test for item loss: items are only removed after the
        // account is credited, so a database failure must leave them alone.
        give(alice, Material.DIAMOND, 10);
        db().disconnect();

        List<String> msgs = run(alice, "deposit 5");
        assertReceived(msgs, "unknown error");
        assertNotReceived(msgs, "You have deposited");
        assertEquals(10, count(alice, Material.DIAMOND), "items must NOT be removed when the deposit fails");
    }

    // ---------- withdraw ----------

    @Test
    void withdrawGivesItemsAndDeductsBalance() {
        setBalance(alice, 10);
        List<String> msgs = run(alice, "withdraw 3");
        assertReceived(msgs, "You have withdrawn 3");
        assertReceived(msgs, "New balance: 7.00");
        assertEquals(3, count(alice, Material.DIAMOND));
    }

    @Test
    void withdrawAllHandlesFractionalBalance() {
        setBalance(alice, 10.5);
        List<String> msgs = run(alice, "withdraw all");
        assertReceived(msgs, "You have withdrawn 10");
        assertReceived(msgs, "New balance: 0.50");
        assertEquals(10, count(alice, Material.DIAMOND));
    }

    @Test
    void withdrawInsufficientFundsGivesNoItems() {
        setBalance(alice, 5);
        List<String> msgs = run(alice, "withdraw 10");
        assertReceived(msgs, "enough funds");
        assertEquals(0, count(alice, Material.DIAMOND));
    }

    @Test
    void withdrawWithBalanceBelowOneExplains() {
        setBalance(alice, 0.5);
        List<String> msgs = run(alice, "withdraw all");
        assertReceived(msgs, "whole item");
        assertEquals(0, count(alice, Material.DIAMOND));
    }

    @Test
    void withdrawWithFullInventoryFails() {
        setBalance(alice, 10);
        fillInventory(alice);
        List<String> msgs = run(alice, "withdraw 1");
        assertReceived(msgs, "inventory space");
        assertEquals(0, count(alice, Material.DIAMOND));
    }

    @Test
    void withdrawWithDatabaseDownDoesNotGiveItems() throws Exception {
        // Regression test for the free-items bug: items are only handed out
        // after the account was actually debited.
        setBalance(alice, 10);
        db().disconnect();

        List<String> msgs = run(alice, "withdraw 5");
        assertReceived(msgs, "unknown error");
        assertNotReceived(msgs, "You have withdrawn");
        assertEquals(0, count(alice, Material.DIAMOND), "no items may appear when the withdrawal fails");
    }

    // ---------- pay ----------

    @Test
    void payTransfersMoneyAndNotifiesBoth() {
        setBalance(alice, 100);
        setBalance(bob, 20);
        List<String> msgs = run(alice, "pay Bob 40");
        assertReceived(msgs, "You sent 40");
        assertReceived(messages(bob), "You received 40");
        assertReceived(run(alice, "balance"), "Your balance: 60.00");
        assertReceived(run(bob, "balance"), "Your balance: 60.00");
    }

    @Test
    void paySelfOfflineAndInsufficientAreRejected() {
        setBalance(alice, 5);
        assertReceived(run(alice, "pay Alice 1"), "pay yourself");
        assertReceived(run(alice, "pay Nobody 1"), "Player not found");
        assertReceived(run(alice, "pay Bob 10"), "enough funds");
        assertReceived(run(alice, "balance"), "Your balance: 5.00");
    }

    @Test
    void payRejectsInvalidAmounts() {
        setBalance(alice, 100);
        for (String bad : new String[]{"NaN", "0", "-5", "1e5"}) {
            List<String> msgs = run(alice, "pay Bob " + bad);
            assertReceived(msgs, "Invalid number: " + bad);
        }
        assertReceived(run(alice, "balance"), "Your balance: 100.00");
    }

    @Test
    void payWithDatabaseDownFailsCleanly() throws Exception {
        setBalance(alice, 100);
        db().disconnect();

        List<String> msgs = run(alice, "pay Bob 10");
        assertReceived(msgs, "unknown error");
        assertNotReceived(msgs, "You sent");
        assertTrue(messages(bob).stream().noneMatch(m -> m.contains("received")),
                "target must not be notified of a failed payment");
    }

    // ---------- eco / balance / baltop / main ----------

    @Test
    void ecoGiveTakeSetFromConsole() {
        assertTrue(server.dispatchCommand(server.getConsoleSender(), "eco give Alice 50"));
        assertReceived(run(alice, "balance"), "Your balance: 50.00");

        assertTrue(server.dispatchCommand(server.getConsoleSender(), "eco take Alice 20"));
        assertReceived(run(alice, "balance"), "Your balance: 30.00");

        assertTrue(server.dispatchCommand(server.getConsoleSender(), "eco set Alice 0"));
        assertReceived(run(alice, "balance"), "Your balance: 0.00");
    }

    @Test
    void balanceOtherRequiresAdminPermission() {
        assertFalse(alice.isOp());
        List<String> msgs = run(alice, "balance Bob");
        assertReceived(msgs, "do not have permission");

        alice.setOp(true);
        assertReceived(run(alice, "balance Bob"), "Bob's Balance: 0.00");
    }

    @Test
    void baltopListsPlayersSortedByBalance() {
        setBalance(alice, 500);
        setBalance(bob, 10);
        List<String> msgs = run(alice, "baltop");
        assertReceived(msgs, "Top Balances");
        assertReceived(msgs, "Alice : 500.00");
        assertReceived(msgs, "Bob : 10.00");
    }

    @Test
    void baltopToggleHidesAndUnhidesPlayer() {
        setBalance(alice, 500);
        setBalance(bob, 10);

        assertReceived(run(alice, "baltop toggle"), "hidden from /baltop");
        List<String> msgs = run(bob, "baltop");
        assertNotReceived(msgs, "Alice");

        assertReceived(run(alice, "baltop toggle"), "now visible");
        assertReceived(run(bob, "baltop"), "Alice");
    }

    @Test
    void mainCommandReloadWorks() {
        alice.setOp(true);
        List<String> msgs = run(alice, "de reload");
        assertReceived(msgs, "reloaded");
    }

    @Test
    void mainCommandUnknownArgumentShowsUsageNotFakeSuccess() {
        // Regression test: unknown subcommands used to reply "reloaded".
        alice.setOp(true);
        List<String> msgs = run(alice, "de garbage");
        assertReceived(msgs, "Usage");
        assertNotReceived(msgs, "reloaded");
    }
}
