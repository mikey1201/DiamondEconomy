package com.mikey1201.utils;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class CurrencyUtils {

    private CurrencyUtils() {
    }

    /**
     * Resolves the configured currency item, falling back to DIAMOND (with a
     * warning) when the configured name is missing or not an item.
     */
    public static Material getCurrencyMaterial(JavaPlugin plugin) {
        String configName = plugin.getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);

        if (material == null || !material.isItem()) {
            plugin.getLogger().warning("Invalid material '" + configName + "' in config.yml. Defaulting to DIAMOND.");
            return Material.DIAMOND;
        }
        return material;
    }
}