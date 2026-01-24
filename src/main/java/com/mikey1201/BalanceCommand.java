package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;

    public BalanceCommand(EconomyProvider economy, MessageManager messages) {
        this.economy = economy;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("errors.player-only"));
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("diamondeconomy.admin")) {
                sender.sendMessage(messages.get("errors.no-permission"));
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                sender.sendMessage(messages.get("errors.player-not-found"));
                return true;
            }
        }

        double balance = economy.getBalance(target);
        String formattedBalance = economy.format(balance);
        String playerName = target.getName() != null ? target.getName() : "Unknown";

        if (sender.equals(target)) {
            sender.sendMessage(messages.get("balance.self", "{amount}", formattedBalance));
        } else {
            sender.sendMessage(messages.get("balance.other", "{player}", playerName, "{amount}", formattedBalance));
        }

        return true;
    }
}