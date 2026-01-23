package com.mikey1201;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final Material currencyItem;

    public WithdrawCommand(EconomyProvider economy, MessageManager messages, Material currencyItem) {
        this.economy = economy;
        this.messages = messages;
        this.currencyItem = currencyItem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("errors.player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(messages.get("errors.usage-amount"));
            return true;
        }

        Player player = (Player) sender;
        int amountToWithdraw;

        double currentBalance = economy.getBalance(player);
        
        if (args[0].equalsIgnoreCase("all")) {
            amountToWithdraw = (int) Math.floor(currentBalance);
        } else {
            try {
                double parsedAmount = Double.parseDouble(args[0]);
                if (parsedAmount <= 0) {
                    player.sendMessage(messages.get("errors.positive-number"));
                    return true;
                }
                if (parsedAmount % 1 != 0) {
                    player.sendMessage(messages.get("errors.fractional-items"));
                    return true;
                }
                amountToWithdraw = (int) parsedAmount;
            } catch (NumberFormatException e) {
                player.sendMessage(messages.get("errors.invalid-number", "{input}", args[0]));
                return true;
            }
        }

        if (amountToWithdraw == 0) {
            if (currentBalance < 1.0) {
                 player.sendMessage(messages.get("withdraw.under-one", "{balance}", economy.format(currentBalance)));
            } else {
                 player.sendMessage(messages.get("errors.positive-number"));
            }
            return true;
        }

        if (amountToWithdraw > currentBalance) {
            player.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        int maxStackSize = currencyItem.getMaxStackSize();
        int fullStacks = amountToWithdraw / maxStackSize;
        int remainder = amountToWithdraw % maxStackSize;
        int slotsNeeded = fullStacks + (remainder > 0 ? 1 : 0);
        
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            }
        }

        if (emptySlots < slotsNeeded) {
            player.sendMessage(messages.get("errors.inventory-full"));
            return true;
        }

        economy.withdrawPlayer(player, amountToWithdraw);

        if (fullStacks > 0) {
            for (int i = 0; i < fullStacks; i++) {
                player.getInventory().addItem(new ItemStack(currencyItem, maxStackSize));
            }
        }
        if (remainder > 0) {
            player.getInventory().addItem(new ItemStack(currencyItem, remainder));
        }

        player.sendMessage(messages.get("withdraw.success", "{amount}", String.valueOf(amountToWithdraw)));
        player.sendMessage(messages.get("withdraw.new-balance", "{balance}", economy.format(economy.getBalance(player))));
        return true;
    }
}