package com.mikey1201.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.managers.DatabaseException;
import com.mikey1201.managers.DatabaseManager;

public class PlayerUtils {

    /**
     * Finds a player by name: an online player, a player the server knows,
     * or failing that, a known economy account (e.g. Towny towns/nations,
     * which only exist as rows in the economy database). Returns null if no
     * such account exists anywhere.
     */
    public static OfflinePlayer getOfflinePlayer(String name, DatabaseManager database) {
        if (name == null) return null;
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        // Check if player has played before (exists in server data) or is online
        if (player != null && (player.hasPlayedBefore() || player.isOnline())) {
            return player;
        }

        try {
            UUID uuid = database.getUUID(name);
            if (uuid != null) {
                return Bukkit.getOfflinePlayer(uuid);
            }
        } catch (DatabaseException ignored) {
            // Lookup failure: treat as unknown account
        }
        return null;
    }

    /**
     * Checks if sender is a player. Returns false and sends message if console.
     */
    public static boolean isPlayer(CommandSender sender, String errorMessage) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(errorMessage);
            return false;
        }
        return true;
    }
}