package com.mikey1201.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

public class EcoSet extends SubCommand {

    private final EconomyProvider economy;
    private final DatabaseManager database;

    public EcoSet(EconomyProvider economy, MessageManager messages, DatabaseManager database) {
        super(messages, "diamondeconomy.admin", false);
        this.economy = economy;
        this.database = database;
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
            amount = InputUtils.parsePositiveDoubleAllowZero(args[2]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[2]));
            return true;
        }

        database.setBalance(target.getUniqueId(), amount);
        sender.sendMessage(messages.get("eco.set", "{player}", targetName, "{amount}", String.valueOf(amount)));
        return true;
    }
}
