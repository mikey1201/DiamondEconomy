package com.mikey1201.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.managers.DatabaseException;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.DatabaseManager.BalanceEntry;
import com.mikey1201.managers.HiddenPlayersManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;

public class BaltopCommand implements CommandExecutor {

    private final DatabaseManager database;
    private final EconomyProvider economy;
    private final MessageManager messages;
    private final HiddenPlayersManager hiddenPlayersManager;

    public BaltopCommand(DatabaseManager database, EconomyProvider economy, MessageManager messages, HiddenPlayersManager hiddenPlayersManager) {
        this.database = database;
        this.economy = economy;
        this.messages = messages;
        this.hiddenPlayersManager = hiddenPlayersManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("diamondeconomy.toggle")) {
                sender.sendMessage(messages.get("errors.no-permission"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("errors.player-only"));
                return true;
            }

            Player player = (Player) sender;
            boolean isNowHidden = hiddenPlayersManager.toggle(player.getUniqueId());
            
            if (isNowHidden) {
                player.sendMessage(messages.get("toggle.hidden"));
            } else {
                player.sendMessage(messages.get("toggle.visible"));
            }
            return true;
        }

        // Uses getTopBalancesWithNames to get stored names from the database
        // so non-player accounts (e.g., Towny towns/nations) display correctly.
        List<BalanceEntry> potentialBalances;
        try {
            potentialBalances = database.getTopBalancesWithNames(15);
        } catch (DatabaseException e) {
            Bukkit.getLogger().warning("DiamondEconomy: /baltop failed: " + e.getMessage());
            sender.sendMessage(messages.get("errors.unknown-error"));
            return true;
        }

        if (potentialBalances.isEmpty()) {
            sender.sendMessage(messages.get("baltop.empty"));
            return true;
        }

        sender.sendMessage(messages.get("baltop.header"));
        
        int count = 0;
        for (BalanceEntry entry : potentialBalances) {
            if (count >= 10) break;

            if (hiddenPlayersManager.isHidden(entry.uuid())) {
                continue;
            }

            // Use the stored name from the database instead of relying on
            // OfflinePlayer.getName(), which returns null for non-player
            // accounts like Towny towns/nations.
            String playerName = entry.name();
            if (playerName == null || playerName.isEmpty()) {
                playerName = "Unknown";
            }

            String formattedBalance = economy.format(entry.balance());
            
            sender.sendMessage(messages.get("baltop.entry", 
                "{rank}", String.valueOf(count + 1), 
                "{player}", playerName,
                "{amount}", formattedBalance
            ));
            
            count++;
        }
        
        if (count == 0) {
             sender.sendMessage(messages.get("baltop.empty"));
        }

        return true;
    }
}
