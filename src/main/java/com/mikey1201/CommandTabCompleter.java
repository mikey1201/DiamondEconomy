package com.mikey1201;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CommandTabCompleter implements TabCompleter {

    private static final List<String> ALL_ARG = Arrays.asList("all");
    private static final List<String> ECO_ACTIONS = Arrays.asList("give", "take", "set");
    private static final List<String> BALTOP_ACTIONS = Arrays.asList("toggle");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("balance")) {
            if (args.length == 1 && sender.hasPermission("diamondeconomy.admin")) {
                return StringUtil.copyPartialMatches(args[0], getOnlinePlayerNames(), new ArrayList<>());
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("deposit") || cmdName.equals("withdraw")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], ALL_ARG, new ArrayList<>());
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("pay")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], getOnlinePlayerNamesExcluding(sender.getName()), new ArrayList<>());
            }
            return Collections.emptyList();
        }

        if (cmdName.equals("eco")) {
            if (args.length == 1 && sender.hasPermission("diamondeconomy.admin")) {
                return StringUtil.copyPartialMatches(args[0], ECO_ACTIONS, new ArrayList<>());
            }
            if (args.length == 2 && sender.hasPermission("diamondeconomy.admin")) {
                return StringUtil.copyPartialMatches(args[0], getOnlinePlayerNames(), new ArrayList<>());
            }
            return Collections.emptyList();
        }

        // Baltop: Suggest "toggle" subcommand
        if (cmdName.equals("baltop")) {
            if (args.length == 1 && sender.hasPermission("diamondeconomy.toggle")) {
                return StringUtil.copyPartialMatches(args[0], BALTOP_ACTIONS, new ArrayList<>());
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNamesExcluding(String excludeName) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.getName().equals(excludeName))
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}