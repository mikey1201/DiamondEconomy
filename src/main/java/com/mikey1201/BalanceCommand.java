package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyProvider economy;

    public BalanceCommand(EconomyProvider economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Target logic: If args provided, check if sender is admin. Otherwise, use sender.
        OfflinePlayer target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("diamondeconomy.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to check other players' balances.");
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || !target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }
        }

        double balance = economy.getBalance(target);
        String playerName = target.getName() != null ? target.getName() : "Unknown";

        sender.sendMessage(ChatColor.AQUA + playerName + "'s Balance: " + ChatColor.GOLD + economy.format(balance));
        return true;
    }
}