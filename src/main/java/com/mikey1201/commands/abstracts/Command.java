package com.mikey1201.commands.abstracts;

import com.mikey1201.managers.Messages;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class Command implements CommandExecutor {

    private final Messages messages;
    private final String permission;
    private final boolean playerOnly;

    public Command(Messages messages, String permission, boolean playerOnly) {
        this.messages = messages;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    public Command(Messages messages) {
        this(messages, null, false);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(messages.get("errors.player-only"));
            return true;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(messages.get("errors.no-permission"));
            return true;
        }

        return execute(sender, label, args);
    }

    public abstract boolean execute(CommandSender sender, String label, String[] args);
}
