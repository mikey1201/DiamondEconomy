package com.mikey1201.hooks;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.providers.EconomyProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultHook {

    public static void hook(Plugin plugin, DatabaseManager database, Logger logger) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault not found! Disabling economy features.");
            return;
        }

        plugin.getServer().getServicesManager().register(
                Economy.class,
                new EconomyProvider(database),
                plugin,
                ServicePriority.Highest
        );
        logger.info("Successfully hooked into Vault!");
    }
}