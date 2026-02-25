package com.mikey1201.providers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.junit.Before;
import org.junit.Test;

import com.mikey1201.managers.DatabaseManager;

import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyProviderTest {

    private DatabaseManager database;
    private EconomyProvider provider;
    private OfflinePlayer player;
    private UUID playerUuid;

    @Before
    public void setUp() {
        database = mock(DatabaseManager.class);
        provider = new EconomyProvider(database);
        player = mock(OfflinePlayer.class);
        playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(player.getName()).thenReturn("TestPlayer");
    }

    @Test
    public void testGetBalance_AccountExists() {
        when(database.hasAccount(playerUuid)).thenReturn(true);
        when(database.getBalance(playerUuid)).thenReturn(100.0);

        assertEquals(100.0, provider.getBalance(player), 0.001);
    }

    @Test
    public void testGetBalance_AccountDoesNotExist() {
        when(database.hasAccount(playerUuid)).thenReturn(false);

        assertEquals(0.0, provider.getBalance(player), 0.001);
    }

    @Test
    public void testWithdraw_Success() {
        when(database.hasAccount(playerUuid)).thenReturn(true);
        when(database.getBalance(playerUuid)).thenReturn(100.0);

        EconomyResponse response = provider.withdrawPlayer(player, 40.0);

        assertTrue(response.transactionSuccess());
        assertEquals(60.0, response.balance, 0.001);
        verify(database).updateBalance(playerUuid, "TestPlayer", 60.0);
    }

    @Test
    public void testWithdraw_InsufficientFunds() {
        when(database.hasAccount(playerUuid)).thenReturn(true);
        when(database.getBalance(playerUuid)).thenReturn(10.0);

        EconomyResponse response = provider.withdrawPlayer(player, 40.0);

        assertFalse(response.transactionSuccess());
        assertEquals("Insufficient funds.", response.errorMessage);
    }

    @Test
    public void testDeposit_Success() {
        when(database.hasAccount(playerUuid)).thenReturn(true);
        when(database.getBalance(playerUuid)).thenReturn(100.0);

        EconomyResponse response = provider.depositPlayer(player, 40.0);

        assertTrue(response.transactionSuccess());
        assertEquals(140.0, response.balance, 0.001);
        verify(database).updateBalance(playerUuid, "TestPlayer", 140.0);
    }
}
