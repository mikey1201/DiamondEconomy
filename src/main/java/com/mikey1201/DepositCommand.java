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

    public DepositCommand(EconomyProvider economy) {
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
        int amountToDeposit;

        if (args[0].equalsIgnoreCase("all")) {
            amountToDeposit = getDiamondCount(player);
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
            player.sendMessage(ChatColor.RED + "You have no diamonds to deposit.");
            return true;
        }

        int diamondsInInventory = getDiamondCount(player);
        if (diamondsInInventory < amountToDeposit) {
            player.sendMessage(ChatColor.RED + "You only have " + ChatColor.AQUA + diamondsInInventory + " ⬧" + ChatColor.RED + " to deposit.");
            return true;
        }

        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, amountToDeposit));
        economy.depositPlayer(player, amountToDeposit);

        player.sendMessage(ChatColor.GREEN + "You have deposited " + ChatColor.AQUA + amountToDeposit + " ⬧.");
        player.sendMessage("New balance: " + ChatColor.AQUA + economy.format(economy.getBalance(player)));
        return true;
    }

    private int getDiamondCount(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                count += item.getAmount();
            }
        }
        return count;
    }
}