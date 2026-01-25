package com.mikey1201.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mikey1201.managers.DatabaseManager;

public class PlayerAccountListener implements Listener {

    private final DatabaseManager database;

    public PlayerAccountListener(DatabaseManager database) {
        this.database = database;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        double currentBalance = database.getBalance(player.getUniqueId());

        database.updateBalance(player.getUniqueId(), player.getName(), currentBalance);
    }
}