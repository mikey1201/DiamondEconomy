package com.mikey1201.commands;

import org.bukkit.command.CommandSender;

import com.mikey1201.DiamondEconomy;
import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.MessageManager;

public class DiamondEconomyCommand extends Command {

    private final DiamondEconomy plugin;

    public DiamondEconomyCommand(DiamondEconomy plugin, MessageManager messages) {
        super(messages, "diamondeconomy.admin", false);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            messages.reloadConfig();
            sender.sendMessage(messages.get("admin.reload-success"));
            return true;
        }

        sender.sendMessage(messages.get("admin.usage"));
        return true;
    }
}
