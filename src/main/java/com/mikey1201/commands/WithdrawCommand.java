package com.mikey1201.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.mikey1201.managers.DatabaseException;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

import net.milkbowl.vault.economy.EconomyResponse;

public class WithdrawCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final JavaPlugin plugin;

    public WithdrawCommand(EconomyProvider economy, MessageManager messages, JavaPlugin plugin) {
        this.economy = economy;
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PlayerUtils.isPlayer(sender, messages.get("errors.player-only"))) {
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(messages.get("errors.usage-amount", "{label}", label));
            return true;
        }

        Player player = (Player) sender;
        int amountToWithdraw;
        
        Material currencyItem = getCurrencyMaterial();
        double currentBalance;
        try {
            currentBalance = economy.getBalance(player);
        } catch (DatabaseException e) {
            e.printStackTrace();
            player.sendMessage(messages.get("errors.unknown-error"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("all")) {
            // Note: the (int) cast saturates at Integer.MAX_VALUE for huge
            // balances; the inventory-space check below rejects such amounts.
            amountToWithdraw = (int) Math.floor(currentBalance);
        } else {
            try {
                // Strict whole-number parse: rejects fractional values, signs,
                // scientific notation, NaN/Infinity, and int overflow.
                amountToWithdraw = InputUtils.parsePositiveWholeInt(args[0]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[0]));
                return true;
            }
        }

        if (amountToWithdraw == 0) {
            if (currentBalance < 1.0) {
                 player.sendMessage(messages.get("withdraw.under-one", "{amount}", economy.format(currentBalance)));
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

        // FIX: withdraw from the account FIRST and only give items if the
        // withdrawal actually succeeded. Previously items were handed out
        // even when the withdrawal failed, duplicating currency.
        EconomyResponse response = economy.withdrawPlayer(player, amountToWithdraw);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            if (economy.failureWasDatabaseError(player, amountToWithdraw)) {
                Bukkit.getLogger().warning("DiamondEconomy: /withdraw failed for " + player.getName() + ": " + response.errorMessage);
                player.sendMessage(messages.get("errors.unknown-error"));
            } else {
                // Lost a race with another transaction (funds were sufficient
                // when we checked a moment ago).
                player.sendMessage(messages.get("errors.insufficient-funds"));
            }
            return true;
        }

        if (fullStacks > 0) {
            for (int i = 0; i < fullStacks; i++) {
                player.getInventory().addItem(new ItemStack(currencyItem, maxStackSize));
            }
        }
        if (remainder > 0) {
            player.getInventory().addItem(new ItemStack(currencyItem, remainder));
        }

        player.sendMessage(messages.get("withdraw.success", "{amount}", String.valueOf(amountToWithdraw)));
        player.sendMessage(messages.get("withdraw.new-balance", "{amount}", economy.format(response.balance)));
        return true;
    }
    
    private Material getCurrencyMaterial() {
        String configName = plugin.getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);
        if (material == null || !material.isItem()) {
            return Material.DIAMOND;
        }
        return material;
    }
}
