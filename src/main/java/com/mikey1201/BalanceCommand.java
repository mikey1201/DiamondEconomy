package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyProvider economy;

    public BalanceCommand(EconomyProvider economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length > 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label);
            return true;
        }

        Player player = (Player) sender;
        if (!economy.hasAccount(player)) {
            economy.createPlayerAccount(player);
        }

        double balance = economy.getBalance(player);
        player.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.AQUA + economy.format(balance));
        return true;
    }
}