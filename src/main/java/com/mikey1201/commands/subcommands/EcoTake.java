package com.mikey1201.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

public class EcoTake extends SubCommand {

    private final EconomyProvider economy;

    public EcoTake(EconomyProvider economy, MessageManager messages) {
        super(messages, "diamondeconomy.admin", false);
        this.economy = economy;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(messages.get("eco.usage"));
            return true;
        }

        String targetName = args[1];
        OfflinePlayer target = PlayerUtils.getOfflinePlayer(targetName);
        if (target == null) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[2]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[2]));
            return true;
        }

        if (economy.getBalance(target) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        economy.withdrawPlayer(target, amount);
        sender.sendMessage(messages.get("eco.take", "{amount}", String.valueOf(amount), "{player}", targetName));
        return true;
    }
}
