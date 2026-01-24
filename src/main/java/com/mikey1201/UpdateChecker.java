package com.mikey1201;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private static final String GITHUB_URL = "https://raw.githubusercontent.com/mikey1201/DiamondEconomy/refs/heads/main/VERSION";

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfig().getBoolean("check-for-updates", true)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(GITHUB_URL);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine();
                reader.close();

                String currentVersion = plugin.getDescription().getVersion();
                plugin.getLogger().info("Checking for update.");
                if (latestVersion != null && !currentVersion.equals(latestVersion)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().warning("A new version of " + plugin.getName() + " is available!");
                    });
                } else {
                    plugin.getLogger().info("Already up to date.");
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}