package com.mikey1201.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.PlayerUtils;

public class BalanceCommand extends Command {

    private final EconomyProvider economy;

    public BalanceCommand(EconomyProvider economy, MessageManager messages) {
        super(messages, null, false);
        this.economy = economy;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
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
            target = PlayerUtils.getOfflinePlayer(args[0]);
            if (target == null) {
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
