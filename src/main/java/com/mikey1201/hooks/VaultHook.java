package com.mikey1201.hooks;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import com.mikey1201.providers.EconomyProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultHook {

    public static void hook(Plugin plugin, EconomyProvider economyProvider, Logger logger) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault not found! Disabling economy features.");
            return;
        }

        plugin.getServer().getServicesManager().register(
                Economy.class,
                economyProvider,
                plugin,
                ServicePriority.Highest
        );
        logger.info("Successfully hooked into Vault!");
    }
}
