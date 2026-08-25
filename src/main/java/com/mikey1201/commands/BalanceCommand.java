package com.mikey1201.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.managers.DatabaseException;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.PlayerUtils;

public class BalanceCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final DatabaseManager database;

    public BalanceCommand(EconomyProvider economy, MessageManager messages, DatabaseManager database) {
        this.economy = economy;
        this.messages = messages;
        this.database = database;
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
            // Documented permission for checking other players' balances
            // (ops inherit it via the admin permission's children).
            if (!sender.hasPermission("diamondeconomy.balance.others")) {
                sender.sendMessage(messages.get("errors.no-permission"));
                return true;
            }
            target = PlayerUtils.getOfflinePlayer(args[0], database);
            if (target == null) {
                sender.sendMessage(messages.get("errors.player-not-found"));
                return true;
            }
        }

        double balance;
        String storedName;
        try {
            balance = economy.getBalance(target);
            storedName = database.getName(target.getUniqueId());
        } catch (DatabaseException e) {
            Bukkit.getLogger().warning("DiamondEconomy: /balance failed for " + target.getName() + ": " + e.getMessage());
            sender.sendMessage(messages.get("errors.unknown-error"));
            return true;
        }
        String formattedBalance = economy.format(balance);
        // Prefer the stored economy name: OfflinePlayer.getName() is null (or
        // synthetic) for accounts that only exist in the database.
        String playerName = storedName != null ? storedName
                : (target.getName() != null ? target.getName() : "Unknown");

        if (sender.equals(target)) {
            sender.sendMessage(messages.get("balance.self", "{amount}", formattedBalance));
        } else {
            sender.sendMessage(messages.get("balance.other", "{player}", playerName, "{amount}", formattedBalance));
        }

        return true;
    }
}
