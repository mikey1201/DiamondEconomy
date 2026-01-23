package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EcoCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final DatabaseManager database;

    public EcoCommand(EconomyProvider economy, DatabaseManager database) {
        this.economy = economy;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("diamondeconomy.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (target == null || !target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }

        switch (action) {
            case "give":
                economy.depositPlayer(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.AQUA + amount + " ⬧" + ChatColor.GREEN + " to " + targetName + ".");
                break;
            case "take":
                if (economy.getBalance(target) < amount) {
                    sender.sendMessage(ChatColor.RED + targetName + " does not have enough funds.");
                    return true;
                }
                economy.withdrawPlayer(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Took " + ChatColor.AQUA + amount + " ⬧" + ChatColor.GREEN + " from " + targetName + ".");
                break;
            case "set":
                try {
                    database.setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(ChatColor.GREEN + "Set " + targetName + "'s balance to " + ChatColor.AQUA + amount + " ⬧.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Error setting balance. Check console.");
                    e.printStackTrace();
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: give, take, or set.");
                return true;
        }

        return true;
    }
}