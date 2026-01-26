package com.mikey1201.commands;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.commands.subcommands.GiveSubCommand;
import com.mikey1201.commands.subcommands.SetSubCommand;
import com.mikey1201.commands.subcommands.TakeSubCommand;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.Messages;
import com.mikey1201.providers.Economy;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EcoCommand extends Command {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final Messages messages;

    public EcoCommand(Economy economy, Messages messages, DatabaseManager database) {
        super(messages, "diamondeconomy.admin", false);
        this.messages = messages;
        subCommands.put("give", new GiveSubCommand(economy, messages));
        subCommands.put("take", new TakeSubCommand(economy, messages));
        subCommands.put("set", new SetSubCommand(database, messages));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.get("eco.usage"));
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(messages.get("eco.unknown-action"));
            return true;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subCommandArgs);

        return true;
    }
}
