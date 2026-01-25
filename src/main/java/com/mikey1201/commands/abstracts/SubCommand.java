package com.mikey1201.commands.abstracts;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private final String name;
    private final String description;
    private final String usage;

    public SubCommand(String name, String description, String usage) {
        this.name = name;
        this.description = description;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public abstract void execute(CommandSender sender, String[] args);
}
