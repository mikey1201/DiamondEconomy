package com.mikey1201.commands;

import com.mikey1201.commands.abstracts.Command;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.mikey1201.managers.Messages;
import com.mikey1201.providers.Economy;
import com.mikey1201.utils.InputUtils;

public class WithdrawCommand extends Command {

    private final Economy economy;
    private final Messages messages;
    private final JavaPlugin plugin;

    public WithdrawCommand(Economy economy, Messages messages, JavaPlugin plugin) {
        super(messages, null, true);
        this.economy = economy;
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(messages.get("errors.usage-amount", "{label}", label));
            return true;
        }

        Player player = (Player) sender;
        int amountToWithdraw;

        Material currencyItem = getCurrencyMaterial();
        double currentBalance = economy.getBalance(player);

        if (args[0].equalsIgnoreCase("all")) {
            amountToWithdraw = (int) Math.floor(currentBalance);
        } else {
            try {
                double parsedAmount = InputUtils.parsePositiveDouble(args[0]);
                InputUtils.checkWholeNumber(parsedAmount); // Throws error if fractional
                amountToWithdraw = (int) parsedAmount;
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
        player.sendMessage(messages.get("withdraw.new-balance", "{amount}", economy.format(economy.getBalance(player))));
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
