package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Map;

public class BaltopCommand implements CommandExecutor {

    private final DatabaseManager database;
    private final EconomyProvider economy;

    public BaltopCommand(DatabaseManager database, EconomyProvider economy) {
        this.database = database;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<String, Double> topBalances = database.getTopBalances(10); // Get top 10

        if (topBalances.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no balances to display yet.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Top Balances ---");
        int rank = 1;
        for (Map.Entry<String, Double> entry : topBalances.entrySet()) {
            sender.sendMessage(String.format("%s%d. %s%s%s: %s",
                    ChatColor.GRAY, rank,
                    ChatColor.GREEN, entry.getKey(),
                    ChatColor.AQUA,
                    economy.format(entry.getValue())
            ));
            rank++;
        }
        return true;
    }
}