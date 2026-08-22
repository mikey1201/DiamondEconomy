package com.mikey1201.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;

import net.milkbowl.vault.economy.EconomyResponse;

public class DepositCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final JavaPlugin plugin;

    public DepositCommand(EconomyProvider economy, MessageManager messages, JavaPlugin plugin) {
        this.economy = economy;
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("errors.player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(messages.get("errors.usage-amount", "{label}", label));
            return true;
        }

        Player player = (Player) sender;
        int amountToDeposit;
        
        // Get material from config dynamically
        Material currencyItem = getCurrencyMaterial();

        if (args[0].equalsIgnoreCase("all")) {
            amountToDeposit = getCurrencyCount(player, currencyItem);
        } else {
            try {
                // Strict whole-number parse: rejects fractional values, signs,
                // scientific notation, NaN/Infinity, and int overflow.
                amountToDeposit = InputUtils.parsePositiveWholeInt(args[0]);
            } catch (IllegalArgumentException e) {
                player.sendMessage(messages.get("errors.invalid-number", "{input}", args[0])); 
                return true;
            }
        }

        if (amountToDeposit == 0) {
            player.sendMessage(messages.get("errors.zero-deposit", "{currency}", currencyItem.name().toLowerCase().replace("_", " ")));
            return true;
        }

        int currencyInInventory = getCurrencyCount(player, currencyItem);
        if (currencyInInventory < amountToDeposit) {
            player.sendMessage(messages.get("errors.insufficient-items", "{amount}", String.valueOf(currencyInInventory)));
            return true;
        }

        // FIX: credit the account FIRST and only remove items from the
        // inventory if the deposit actually succeeded. Previously items were
        // removed before the deposit, so a failed database write silently
        // destroyed the deposited items.
        EconomyResponse response = economy.depositPlayer(player, amountToDeposit);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            Bukkit.getLogger().warning("DiamondEconomy: /deposit failed for " + player.getName() + ": " + response.errorMessage);
            player.sendMessage(messages.get("errors.unknown-error"));
            return true;
        }

        // Items are removed only after the balance was credited. The count
        // check above and this removal run synchronously on the main thread,
        // so the inventory cannot change in between.
        removeItemsByType(player, currencyItem, amountToDeposit);

        player.sendMessage(messages.get("deposit.success", "{amount}", String.valueOf(amountToDeposit)));
        player.sendMessage(messages.get("deposit.new-balance", "{amount}", economy.format(response.balance)));
        return true;
    }

    private int getCurrencyCount(Player player, Material material) {
        int count = 0;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Removes items by type only, ignoring metadata (display names, enchantments, etc.)
     * This fixes the issue where renamed diamonds in an anvil weren't being removed during deposit.
     * @param player The player to remove items from
     * @param material The material type to remove
     * @param amount The total amount to remove
     */
    private void removeItemsByType(Player player, Material material, int amount) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        int remaining = amount;

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    // Remove the entire stack
                    player.getInventory().setItem(i, null);
                    remaining -= itemAmount;
                } else {
                    // Remove partial amount from the stack
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
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
