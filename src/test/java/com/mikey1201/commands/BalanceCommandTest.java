package com.mikey1201.commands;

import com.mikey1201.providers.Economy;
import com.mikey1201.managers.Messages;
import com.mikey1201.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
public class BalanceCommandTest {

    @Mock
    private Economy economy;

    @Mock
    private Messages messages;

    @InjectMocks
    private BalanceCommand balanceCommand;

    @Mock
    private Player player;

    @Mock
    private OfflinePlayer offlinePlayer;

    @Mock
    private CommandSender commandSender;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(PlayerUtils.class);
    }

    @Test
    public void testBalanceSelf() {
        when(economy.getBalance(player)).thenReturn(100.0);
        when(economy.format(100.0)).thenReturn("100 Diamonds");
        when(messages.get("balance.self", "{amount}", "100 Diamonds")).thenReturn("Your balance is 100 Diamonds");

        balanceCommand.execute(player, "balance", new String[]{});

        verify(player).sendMessage("Your balance is 100 Diamonds");
    }

    @Test
    public void testBalanceOther() {
        when(PlayerUtils.getOfflinePlayer("testPlayer")).thenReturn(offlinePlayer);
        when(commandSender.hasPermission("diamondeconomy.admin")).thenReturn(true);
        when(economy.getBalance(offlinePlayer)).thenReturn(100.0);
        when(economy.format(100.0)).thenReturn("100 Diamonds");
        when(offlinePlayer.getName()).thenReturn("testPlayer");
        when(messages.get("balance.other", "{player}", "testPlayer", "{amount}", "100 Diamonds"))
                .thenReturn("testPlayer's balance is 100 Diamonds");

        balanceCommand.execute(commandSender, "balance", new String[]{"testPlayer"});

        verify(commandSender).sendMessage("testPlayer's balance is 100 Diamonds");
    }
}
