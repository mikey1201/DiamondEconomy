package com.mikey1201;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private static final String GITHUB_URL = "https://raw.githubusercontent.com/mikey1201/DiamondEconomy/main/pom.xml";

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

                String rawLatest = extractVersion(content.toString());
                String latestVersion = sanitizeVersion(rawLatest);
                String currentVersion = sanitizeVersion(plugin.getDescription().getVersion());

                if (latestVersion == null) {
                    plugin.getLogger().warning("Could not find version in pom.xml");
                    return;
                }

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
        String artifactId = "<artifactId>DiamondEconomy</artifactId>";
        int index = xml.indexOf(artifactId);

        if (index == -1) {
            return extractFirstVersion(xml);
        }

        String searchArea = xml.substring(index, Math.min(index + 500, xml.length()));

        int start = searchArea.indexOf("<version>");
        int end = searchArea.indexOf("</version>");

        if (start != -1 && end != -1) {
            return searchArea.substring(start + 9, end).trim();
        }

        return null;
    }

    private String extractFirstVersion(String xml) {
        String startTag = "<version>";
        String endTag = "</version>";
        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);

        if (start != -1 && end != -1 && end > start) {
            return xml.substring(start + startTag.length(), end).trim();
        }
        return null;
    }

    private String sanitizeVersion(String version) {
        if (version == null) return null;
        version = version.trim();
        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1).trim();
        }
        if (version.endsWith("-SNAPSHOT")) {
            version = version.substring(0, version.length() - 9).trim();
        }
        return version;
    }
}