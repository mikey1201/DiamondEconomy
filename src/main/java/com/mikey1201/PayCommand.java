package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final EconomyProvider economy;

    public PayCommand(EconomyProvider economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <amount>");
            return true;
        }

        Player payer = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (target == null || !economy.hasAccount(target)) {
            payer.sendMessage(ChatColor.RED + "Player not found or has no account.");
            return true;
        }

        if (payer.getUniqueId().equals(target.getUniqueId())) {
            payer.sendMessage(ChatColor.RED + "You cannot pay yourself.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                payer.sendMessage(ChatColor.RED + "Payment amount must be positive.");
                return true;
            }
            amount = Math.round(amount * 100.0) / 100.0;

        } catch (NumberFormatException e) {
            payer.sendMessage(ChatColor.RED + "Invalid amount specified.");
            return true;
        }

        if (!economy.has(payer, amount)) {
            payer.sendMessage(ChatColor.RED + "You do not have enough funds to make this payment.");
            return true;
        }

        economy.withdrawPlayer(payer, amount);
        economy.depositPlayer(target, amount);

        payer.sendMessage(ChatColor.GREEN + "You paid " + ChatColor.AQUA + economy.format(amount) + ChatColor.GREEN + " to " + target.getName() + ".");

        if (target.isOnline()) {
            ((Player)target).sendMessage(ChatColor.GREEN + "You have received " + ChatColor.AQUA + economy.format(amount) + ChatColor.GREEN + " from " + payer.getName() + ".");
        }

        return true;
    }
}