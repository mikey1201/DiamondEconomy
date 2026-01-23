package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiamondEconomyCommand implements CommandExecutor {

    private final DiamondEconomy plugin;
    private final MessageManager messages;

    public DiamondEconomyCommand(DiamondEconomy plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("diamondeconomy.admin")) {
            sender.sendMessage(messages.get("errors.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "DiamondEconomy version " + plugin.getDescription().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            
            messages.reloadConfig();
            
            sender.sendMessage(messages.get("reload.success"));
            return true;
        }

        sender.sendMessage(messages.get("reload.success"));
        return true;
    }
}