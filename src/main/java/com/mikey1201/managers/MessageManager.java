package com.mikey1201.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;
    private final File messagesFile;
    private String currencySymbol;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        // Load currency symbol from config
        this.currencySymbol = plugin.getConfig().getString("currency-symbol", "⬧");
        loadMessages();
    }

    public void reloadConfig() {
        this.currencySymbol = plugin.getConfig().getString("currency-symbol", "⬧");
        loadMessages();
    }

    public void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Fall back to the bundled messages.yml for any keys missing from the
        // user's file (e.g. keys added by a plugin update), so upgraded
        // installs never see "Missing message: ..." placeholders.
        try (InputStream bundled = plugin.getResource("messages.yml")) {
            if (bundled != null) {
                messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(bundled, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load bundled message defaults: " + e.getMessage());
        }
    }

    public String get(String path, String... replacements) {
        String message = messagesConfig.getString(path);

        if (message == null) {
            return "Missing message: " + path;
        }

        // Auto-replace the {symbol} placeholder
        message = message.replace("{symbol}", currencySymbol);

        // Handle varargs replacements (key, value, key, value)
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String get(String path) {
        return get(path, (String[]) null);
    }
}