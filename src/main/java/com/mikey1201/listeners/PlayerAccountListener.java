package com.mikey1201.listeners;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mikey1201.managers.DatabaseException;
import com.mikey1201.managers.DatabaseManager;

public class PlayerAccountListener implements Listener {

    private final DatabaseManager database;
    private final Logger logger;

    public PlayerAccountListener(DatabaseManager database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            double currentBalance = database.getBalance(player.getUniqueId());
            database.updateBalance(player.getUniqueId(), player.getName(), currentBalance);
        } catch (DatabaseException e) {
            // Never let a database failure throw inside the join event; log it instead.
            logger.warning("Could not sync account for " + player.getName() + ": " + e.getMessage());
        }
    }
}
