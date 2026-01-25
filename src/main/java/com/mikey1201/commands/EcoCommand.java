package com.mikey1201.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

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
        
        OfflinePlayer target = PlayerUtils.getOfflinePlayer(targetName);
        if (target == null) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        double amount;
        try {
            if (action.equals("set")) {
                amount = InputUtils.parsePositiveDoubleAllowZero(args[2]);
            } else {
                amount = InputUtils.parsePositiveDouble(args[2]);
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[2]));
            return true;
        }

        switch (action) {
            case "give":
                economy.depositPlayer(target, amount);
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
                database.setBalance(target.getUniqueId(), amount);
                sender.sendMessage(messages.get("eco.set", "{player}", targetName, "{amount}", String.valueOf(amount)));
                break;
            default:
                sender.sendMessage(messages.get("eco.unknown-action"));
                return true;
        }

        return true;
    }
}