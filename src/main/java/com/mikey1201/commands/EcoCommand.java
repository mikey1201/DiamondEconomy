package com.mikey1201.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;

public class EcoCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final DatabaseManager database;

    public EcoCommand(EconomyProvider economy, MessageManager messages, DatabaseManager database) {
        this.economy = economy;
        this.messages = messages;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("diamondeconomy.admin")) {
            sender.sendMessage(messages.get("errors.no-permission"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(messages.get("eco.usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            
            // FIX: Changed from <= 0 to < 0 to allow setting balance to 0
            if (amount < 0) {
                sender.sendMessage(messages.get("errors.positive-number"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[2]));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        switch (action) {
            case "give":
                economy.depositPlayer(target, amount);
                // FIX: Pass amount and player name to placeholders
                sender.sendMessage(messages.get("eco.give", "{amount}", String.valueOf(amount), "{player}", targetName));
                break;
            case "take":
                if (economy.getBalance(target) < amount) {
                    sender.sendMessage(messages.get("errors.insufficient-funds"));
                    return true;
                }
                economy.withdrawPlayer(target, amount);
                sender.sendMessage(messages.get("eco.take", "{amount}", String.valueOf(amount), "{player}", targetName));
                break;
            case "set":
                try {
                    database.setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(messages.get("eco.set", "{player}", targetName, "{amount}", String.valueOf(amount)));
                } catch (Exception e) {
                    sender.sendMessage(messages.get("errors.unknown-error")); // Ensure this key exists in messages.yml or remove it
                    e.printStackTrace();
                }
                break;
            default:
                sender.sendMessage(messages.get("eco.unknown-action"));
                return true;
        }

        return true;
    }
}