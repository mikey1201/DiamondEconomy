package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DepositCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final Material currencyItem;

    // UPDATED: Accept Material in constructor
    public DepositCommand(EconomyProvider economy, Material currencyItem) {
        this.economy = economy;
        this.currencyItem = currencyItem;
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
        int amountToDeposit;

        if (args[0].equalsIgnoreCase("all")) {
            amountToDeposit = getCurrencyCount(player);
        } else {
            try {
                amountToDeposit = Integer.parseInt(args[0]);
                if (amountToDeposit <= 0) {
                    player.sendMessage(ChatColor.RED + "Please enter a positive number.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number: " + args[0]);
                return true;
            }
        }

        if (amountToDeposit == 0) {
            player.sendMessage(ChatColor.RED + "You have no " + currencyItem.name().toLowerCase().replace("_", " ") + "s to deposit.");
            return true;
        }

        int currencyInInventory = getCurrencyCount(player);
        if (currencyInInventory < amountToDeposit) {
            player.sendMessage(ChatColor.RED + "You only have " + ChatColor.AQUA + currencyInInventory + " ⬧" + ChatColor.RED + " to deposit.");
            return true;
        }

        // FIX: Use currencyItem variable
        player.getInventory().removeItem(new ItemStack(currencyItem, amountToDeposit));
        economy.depositPlayer(player, amountToDeposit);

        player.sendMessage(ChatColor.GREEN + "You have deposited " + ChatColor.AQUA + amountToDeposit + " ⬧.");
        player.sendMessage("New balance: " + ChatColor.AQUA + economy.format(economy.getBalance(player)));
        return true;
    }

    private int getCurrencyCount(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            // FIX: Check against currencyItem
            if (item != null && item.getType() == currencyItem) {
                count += item.getAmount();
            }
        }
        return count;
    }
}