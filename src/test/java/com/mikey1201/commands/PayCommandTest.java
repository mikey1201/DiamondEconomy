package com.mikey1201.commands;

import com.mikey1201.managers.Messages;
import com.mikey1201.providers.Economy;
import org.bukkit.Bukkit;
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
@PrepareForTest(Bukkit.class)
public class PayCommandTest {

    @Mock
    private Economy economy;

    @Mock
    private Messages messages;

    @InjectMocks
    private PayCommand payCommand;

    @Mock
    private Player sender;

    @Mock
    private Player target;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Bukkit.class);
    }

    @Test
    public void testPay() {
        when(Bukkit.getPlayer("targetPlayer")).thenReturn(target);
        when(target.isOnline()).thenReturn(true);
        when(economy.getBalance(sender)).thenReturn(200.0);

        payCommand.execute(sender, "pay", new String[]{"targetPlayer", "100"});

        verify(economy).withdrawPlayer(sender, 100);
        verify(economy).depositPlayer(target, 100);
    }
}
