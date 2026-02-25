package com.mikey1201.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mikey1201.DiamondEconomy;
import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;

public class WithdrawCommand extends Command {

    private final EconomyProvider economy;
    private final DiamondEconomy plugin;

    public WithdrawCommand(EconomyProvider economy, MessageManager messages, DiamondEconomy plugin) {
        super(messages, null, true);
        this.economy = economy;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length != 1) {
            sender.sendMessage(messages.get("withdraw.usage"));
            return true;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[0]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[0]));
            return true;
        }

        if (economy.getBalance(player) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        Material currencyMat = getCurrencyMaterial();
        ItemStack item = new ItemStack(currencyMat, (int) amount);

        if (!hasInventorySpace(player, item)) {
            sender.sendMessage(messages.get("errors.no-inventory-space"));
            return true;
        }

        economy.withdrawPlayer(player, amount);
        player.getInventory().addItem(item);

        sender.sendMessage(messages.get("withdraw.success", "{amount}", String.valueOf(amount)));

        return true;
    }

    private Material getCurrencyMaterial() {
        String configName = plugin.getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);
        return (material == null) ? Material.DIAMOND : material;
    }

    private boolean hasInventorySpace(Player player, ItemStack item) {
        return player.getInventory().firstEmpty() != -1;
    }
}
