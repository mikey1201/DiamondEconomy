package com.mikey1201.commands.subcommands;

import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TakeSubCommand extends SubCommand {

    private final EconomyProvider economy;
    private final MessageManager messages;

    public TakeSubCommand(EconomyProvider economy, MessageManager messages) {
        super("take", "Takes money from a player.", "/eco take <player> <amount>");
        this.economy = economy;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(messages.get("eco.usage"));
            return;
        }

        String targetName = args[0];
        OfflinePlayer target = PlayerUtils.getOfflinePlayer(targetName);
        if (target == null) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[1]));
            return;
        }

        if (economy.getBalance(target) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return;
        }

        economy.withdrawPlayer(target, amount);
        sender.sendMessage(messages.get("eco.take", "{amount}", String.valueOf(amount), "{player}", targetName));
    }
}
