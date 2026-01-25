package com.mikey1201.commands.subcommands;

import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class GiveSubCommand extends SubCommand {

    private final EconomyProvider economy;
    private final MessageManager messages;

    public GiveSubCommand(EconomyProvider economy, MessageManager messages) {
        super("give", "Gives a player money.", "/eco give <player> <amount>");
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

        economy.depositPlayer(target, amount);
        sender.sendMessage(messages.get("eco.give", "{amount}", String.valueOf(amount), "{player}", targetName));
    }
}
