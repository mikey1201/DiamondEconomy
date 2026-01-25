package com.mikey1201.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;

public class DepositCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final JavaPlugin plugin;

    // CONSTRUCTOR UPDATED: Accepts JavaPlugin instead of Material
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
                double parsedAmount = Double.parseDouble(args[0]);
                if (parsedAmount <= 0) {
                    player.sendMessage(messages.get("errors.positive-number"));
                    return true;
                }
                if (parsedAmount % 1 != 0) {
                    player.sendMessage(messages.get("errors.fractional-items"));
                    return true;
                }
                amountToDeposit = (int) parsedAmount;
            } catch (NumberFormatException e) {
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

        player.getInventory().removeItem(new ItemStack(currencyItem, amountToDeposit));
        economy.depositPlayer(player, amountToDeposit);

        player.sendMessage(messages.get("deposit.success", "{amount}", String.valueOf(amountToDeposit)));
        player.sendMessage(messages.get("deposit.new-balance", "{amount}", economy.format(economy.getBalance(player))));
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
    
    private Material getCurrencyMaterial() {
        String configName = plugin.getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);
        if (material == null || !material.isItem()) {
            return Material.DIAMOND;
        }
        return material;
    }
}