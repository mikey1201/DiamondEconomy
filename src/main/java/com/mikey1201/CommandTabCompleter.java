package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private static final List<String> ALL_ARG = Arrays.asList("all");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("deposit") || cmdName.equals("withdraw")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], ALL_ARG, new ArrayList<>());
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("pay")) {
            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getName().equals(sender.getName())) {
                        playerNames.add(player.getName());
                    }
                }
                return StringUtil.copyPartialMatches(args[0], playerNames, new ArrayList<>());
            }
            if (args.length == 2) {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }
}