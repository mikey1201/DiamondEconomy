package com.mikey1201.commands.abstracts;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.managers.MessageManager;

public abstract class SubCommand {

    protected final MessageManager messages;
    private final String permission;
    private final boolean playerOnly;

    public SubCommand(MessageManager messages, String permission, boolean playerOnly) {
        this.messages = messages;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    public boolean onCommand(CommandSender sender, String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(messages.get("errors.no-permission"));
            return true;
        }

        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(messages.get("errors.player-only"));
            return true;
        }

        return execute(sender, args);
    }

    public abstract boolean execute(CommandSender sender, String[] args);
}
