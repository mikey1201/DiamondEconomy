package com.mikey1201.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

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
                String content;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        openConnection().getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    content = sb.toString();
                }

                String latestVersion = sanitizeVersion(extractVersion(content));
                String currentVersion = sanitizeVersion(plugin.getDescription().getVersion());

                if (latestVersion == null || currentVersion == null) {
                    plugin.getLogger().warning("Could not determine latest version from pom.xml");
                    return;
                }

                if (compareVersions(currentVersion, latestVersion) < 0) {
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getLogger().warning(
                            "A new version of " + plugin.getName() + " is available! (" + latestVersion + ")"));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            plugin.getLogger().info("You are running the latest version!"));
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private URLConnection openConnection() throws Exception {
        URLConnection connection = URI.create(GITHUB_URL).toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    /**
     * Numeric dotted-version comparison (unlike String.compareTo, which would
     * consider "1.2.2" newer than "1.10").
     *
     * @return negative if {@code a} is older than {@code b}, 0 if equal,
     *         positive if newer
     */
    static int compareVersions(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        int length = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < length; i++) {
            int va = i < partsA.length ? leadingNumber(partsA[i]) : 0;
            int vb = i < partsB.length ? leadingNumber(partsB[i]) : 0;
            if (va != vb) {
                return Integer.compare(va, vb);
            }
        }
        return 0;
    }

    /** Leading numeric part of a version segment ("10" in "10-beta"), or 0. */
    private static int leadingNumber(String segment) {
        int end = 0;
        while (end < segment.length() && Character.isDigit(segment.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(segment.substring(0, end));
        } catch (NumberFormatException e) {
            return 0;
        }
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