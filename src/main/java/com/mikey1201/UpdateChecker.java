package com.mikey1201;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private static final String GITHUB_URL = "https://raw.githubusercontent.com/mikey1201/Coordy/refs/heads/main/pom.xml";

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfig().getBoolean("check-for-updates", true)) {
            return;
        }

        plugin.getLogger().info("Checking for updates...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(GITHUB_URL);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000); 
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                String latestVersion = extractVersion(content.toString());

                if (latestVersion == null) {
                    plugin.getLogger().warning("Could not find version in pom.xml");
                    return;
                }

                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equals(latestVersion)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().warning("A new version of " + plugin.getName() + " is available!");
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().info("You are running the latest version!");
                    });
                }

            } catch (Exception e) {
                plugin.getLogger().fine("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String extractVersion(String xml) {
        String startTag = "<version>";
        String endTag = "</version>";
        
        int lastStart = xml.lastIndexOf(startTag);
        int lastEnd = xml.lastIndexOf(endTag);

        if (lastStart != -1 && lastEnd != -1 && lastEnd > lastStart) {
            return xml.substring(lastStart + startTag.length(), lastEnd).trim();
        }
        return null;
    }
}