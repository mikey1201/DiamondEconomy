package com.mikey1201;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final Material currencyItem;

    public WithdrawCommand(EconomyProvider economy, Material currencyItem) {
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
        int amountToWithdraw;

        double currentBalance = economy.getBalance(player);
        
        if (args[0].equalsIgnoreCase("all")) {
            amountToWithdraw = (int) currentBalance; // Assuming 1 currency = 1 item
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

        if (amountToWithdraw > currentBalance) {
            player.sendMessage(ChatColor.RED + "You only have " + ChatColor.AQUA + (int)currentBalance + " ⬧" + ChatColor.RED + " to withdraw.");
            return true;
        }

        // Calculate how many slots are needed (Max stack size depends on material)
        int maxStackSize = currencyItem.getMaxStackSize();
        int fullStacks = amountToWithdraw / maxStackSize;
        int remainder = amountToWithdraw % maxStackSize;

        // Check for inventory space
        int slotsNeeded = fullStacks + (remainder > 0 ? 1 : 0);
        
        // Count empty slots in player inventory
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            } else if (item.getType() == currencyItem) {
                // If there are partial stacks of the currency, we might be able to fit more without new slots
                // For simplicity in this logic, we check for empty slots. 
                // A more robust check would check remaining space in existing partial stacks.
            }
        }

        // Simple space check: if we need more slots than are empty, deny.
        // Note: This is a basic check. Realistically, we should check if we can fit in existing partial stacks too.
        if (emptySlots < slotsNeeded) {
            player.sendMessage(ChatColor.RED + "You do not have enough inventory space.");
            return true;
        }

        // Withdraw logic
        economy.withdrawPlayer(player, amountToWithdraw);

        // Give items
        if (fullStacks > 0) {
            for (int i = 0; i < fullStacks; i++) {
                player.getInventory().addItem(new ItemStack(currencyItem, maxStackSize));
            }
        }
        if (remainder > 0) {
            player.getInventory().addItem(new ItemStack(currencyItem, remainder));
        }

        player.sendMessage(ChatColor.GREEN + "You have withdrawn " + ChatColor.AQUA + amountToWithdraw + " ⬧.");
        player.sendMessage("New balance: " + ChatColor.AQUA + economy.format(economy.getBalance(player)));
        return true;
    }
}