package com.mikey1201.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerUtils {

    /**
     * Finds a player by name. Returns null if not found or invalid.
     */
    public static OfflinePlayer getOfflinePlayer(String name) {
        if (name == null) return null;
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        
        // Check if player has played before (exists in server data) or is online
        if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
            return null;
        }
        return player;
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