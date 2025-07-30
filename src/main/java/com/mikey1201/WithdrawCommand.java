package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class WithdrawCommand implements CommandExecutor {

    private final EconomyProvider economy;

    public WithdrawCommand(EconomyProvider economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <amount|all>");
            return true;
        }

        Player player = (Player) sender;
        double currentBalance = economy.getBalance(player);
        int amountToWithdraw;

        if (args[0].equalsIgnoreCase("all")) {
            amountToWithdraw = (int) currentBalance; // Truncate fractional part
        } else {
            try {
                amountToWithdraw = Integer.parseInt(args[0]);
                if (amountToWithdraw <= 0) {
                    player.sendMessage(ChatColor.RED + "Please enter a positive number.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number: " + args[0]);
                return true;
            }
        }

        if (amountToWithdraw == 0) {
            player.sendMessage(ChatColor.RED + "You have less than 1.00 Diamond in your account to withdraw.");
            return true;
        }

        if (currentBalance < amountToWithdraw) {
            player.sendMessage(ChatColor.RED + "You do not have enough funds to withdraw that amount.");
            return true;
        }

        economy.withdrawPlayer(player, amountToWithdraw);

        ItemStack diamonds = new ItemStack(Material.DIAMOND, amountToWithdraw);
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(diamonds);
        if (!remaining.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), remaining.get(0));
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so the diamonds were dropped on the ground.");
        }

        player.sendMessage(ChatColor.GREEN + "You have withdrawn " + ChatColor.AQUA + amountToWithdraw + " ⬧.");
        player.sendMessage("New balance: " + ChatColor.AQUA + economy.format(economy.getBalance(player)));
        return true;
    }
}