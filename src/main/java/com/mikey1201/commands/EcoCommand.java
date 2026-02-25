package com.mikey1201.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.commands.abstracts.SubCommand;
import com.mikey1201.commands.subcommands.EcoGive;
import com.mikey1201.commands.subcommands.EcoSet;
import com.mikey1201.commands.subcommands.EcoTake;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;

public class EcoCommand extends Command {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public EcoCommand(EconomyProvider economy, MessageManager messages, DatabaseManager database) {
        super(messages, "diamondeconomy.admin", false);
        subCommands.put("give", new EcoGive(economy, messages));
        subCommands.put("take", new EcoTake(economy, messages));
        subCommands.put("set", new EcoSet(economy, messages, database));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(messages.get("eco.usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(action);

        if (subCommand == null) {
            sender.sendMessage(messages.get("eco.unknown-action"));
            return true;
        }

        return subCommand.onCommand(sender, args);
    }
}
