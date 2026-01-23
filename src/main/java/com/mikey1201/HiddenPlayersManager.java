package com.mikey1201;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class HiddenPlayersManager {

    private final JavaPlugin plugin;
    private final File file;
    private final Set<UUID> hiddenPlayers;

    public HiddenPlayersManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "hidden_players.json");
        this.hiddenPlayers = new HashSet<>();
        load();
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            save();
            return false;
        } else {
            hiddenPlayers.add(uuid);
            save();
            return true;
        }
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                return;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create hidden_players.json", e);
            }
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            content = content.trim();
            if (content.isEmpty() || content.equals("[]")) return;

            content = content.substring(1, content.length() - 1);
            
            if (content.trim().isEmpty()) return;

            String[] uuidStrings = content.split(",");
            for (String s : uuidStrings) {
                try {
                    String trimmed = s.trim().replace("\"", "");
                    if (!trimmed.isEmpty()) {
                        hiddenPlayers.add(UUID.fromString(trimmed));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipping invalid UUID in hidden_players.json: " + s);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load hidden_players.json", e);
        }
    }

    private void save() {
        try {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (UUID uuid : hiddenPlayers) {
                if (!first) json.append(",");
                json.append("\"").append(uuid.toString()).append("\"");
                first = false;
            }
            json.append("]");
            
            Files.write(file.toPath(), json.toString().getBytes());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save hidden_players.json", e);
        }
    }
}