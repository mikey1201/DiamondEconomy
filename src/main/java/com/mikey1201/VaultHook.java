package com.mikey1201;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import net.milkbowl.vault.economy.Economy;

import java.util.logging.Logger;

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