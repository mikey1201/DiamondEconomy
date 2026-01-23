package com.mikey1201;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        Map<UUID, Double> potentialBalances = database.getTopBalances(15); 

        if (potentialBalances.isEmpty()) {
            sender.sendMessage(messages.get("baltop.empty"));
            return true;
        }

        sender.sendMessage(messages.get("baltop.header"));
        
        int count = 0;
        for (Map.Entry<UUID, Double> entry : potentialBalances.entrySet()) {
            if (count >= 10) break;

            UUID uuid = entry.getKey();

            if (hiddenPlayersManager.isHidden(uuid)) {
                continue;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            double balance = entry.getValue();
            
            String playerName = player.getName() != null ? player.getName() : "Unknown";
            String formattedBalance = economy.format(balance);
            
            sender.sendMessage(messages.get("baltop.entry", 
                "{rank}", String.valueOf(count + 1), 
                "{player}", playerName,
                "{balance}", formattedBalance
            ));
            
            count++;
        }
        
        if (count == 0) {
             sender.sendMessage(messages.get("baltop.empty"));
        }

        return true;
    }
}