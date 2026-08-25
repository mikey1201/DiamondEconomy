package com.mikey1201.managers;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Tracks players hidden from /baltop, persisted as a JSON array of UUID
 * strings in hidden_players.json.
 *
 * The file is written atomically (temp file + atomic move), so a crash
 * mid-save can never truncate it. A corrupt file is preserved as
 * hidden_players.json.corrupt rather than silently deleted.
 */
public class HiddenPlayersManager {

    private static final String FILE_NAME = "hidden_players.json";

    private final JavaPlugin plugin;
    private final File file;
    private final Set<UUID> hiddenPlayers;
    private final Gson gson = new Gson();

    public HiddenPlayersManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        this.hiddenPlayers = new HashSet<>();
        load();
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        boolean nowHidden;
        if (hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            nowHidden = false;
        } else {
            hiddenPlayers.add(uuid);
            nowHidden = true;
        }
        save();
        return nowHidden;
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + FILE_NAME, e);
            }
            return;
        }

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            // Gson returns null for an empty file (fresh installs)
            Set<UUID> loaded = gson.fromJson(reader, new TypeToken<HashSet<UUID>>() { }.getType());
            if (loaded != null) {
                hiddenPlayers.addAll(loaded);
            }
        } catch (JsonParseException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load " + FILE_NAME
                    + "; starting empty and preserving the file as " + FILE_NAME + ".corrupt", e);
            try {
                Files.move(file.toPath(), file.toPath().resolveSibling(FILE_NAME + ".corrupt"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveFailed) {
                plugin.getLogger().log(Level.WARNING, "Could not preserve the corrupt " + FILE_NAME, moveFailed);
            }
        }
    }

    private void save() {
        Path target = file.toPath();
        Path tmp = target.resolveSibling(FILE_NAME + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            gson.toJson(hiddenPlayers, writer);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Filesystem without atomic move support: plain replace is
                // still far safer than writing in place.
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + FILE_NAME, e);
        }
    }
}