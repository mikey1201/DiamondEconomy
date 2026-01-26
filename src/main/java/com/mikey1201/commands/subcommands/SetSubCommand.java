package com.mikey1201.commands.subcommands;

import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.Messages;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class SetSubCommand extends SubCommand {

    private final DatabaseManager database;
    private final Messages messages;

    public SetSubCommand(DatabaseManager database, Messages messages) {
        super("set", "Sets a player's balance.", "/eco set <player> <amount>");
        this.database = database;
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
            amount = InputUtils.parsePositiveDoubleAllowZero(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[1]));
            return;
        }

        database.setBalance(target.getUniqueId(), amount);
        sender.sendMessage(messages.get("eco.set", "{player}", targetName, "{amount}", String.valueOf(amount)));
    }
}
