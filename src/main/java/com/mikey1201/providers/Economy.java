package com.mikey1201.providers;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

public interface Economy {
    boolean hasAccount(OfflinePlayer player);
    double getBalance(OfflinePlayer player);
    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
    EconomyResponse depositPlayer(OfflinePlayer player, double amount);
    String format(double amount);
}
