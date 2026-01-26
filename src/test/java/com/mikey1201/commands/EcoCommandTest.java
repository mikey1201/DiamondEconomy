package com.mikey1201.commands;

import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.Messages;
import com.mikey1201.providers.Economy;
import com.mikey1201.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PlayerUtils.class)
public class EcoCommandTest {

    @Mock
    private Economy economy;

    @Mock
    private Messages messages;

    @Mock
    private DatabaseManager databaseManager;

    @InjectMocks
    private EcoCommand ecoCommand;

    @Mock
    private CommandSender commandSender;

    @Mock
    private OfflinePlayer offlinePlayer;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(PlayerUtils.class);
    }

    @Test
    public void testEcoGive() {
        when(PlayerUtils.getOfflinePlayer("testPlayer")).thenReturn(offlinePlayer);
        when(commandSender.hasPermission("diamondeconomy.admin")).thenReturn(true);
        ecoCommand.execute(commandSender, "eco", new String[]{"give", "testPlayer", "100"});
        verify(economy).depositPlayer(offlinePlayer, 100);
    }

    @Test
    public void testEcoTake() {
        when(PlayerUtils.getOfflinePlayer("testPlayer")).thenReturn(offlinePlayer);
        when(commandSender.hasPermission("diamondeconomy.admin")).thenReturn(true);
        when(economy.getBalance(offlinePlayer)).thenReturn(200.0);
        ecoCommand.execute(commandSender, "eco", new String[]{"take", "testPlayer", "100"});
        verify(economy).withdrawPlayer(offlinePlayer, 100);
    }

    @Test
    public void testEcoSet() {
        when(PlayerUtils.getOfflinePlayer("testPlayer")).thenReturn(offlinePlayer);
        when(commandSender.hasPermission("diamondeconomy.admin")).thenReturn(true);
        ecoCommand.execute(commandSender, "eco", new String[]{"set", "testPlayer", "100"});
        verify(databaseManager).setBalance(offlinePlayer.getUniqueId(), 100);
    }
}
