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

public class DepositCommand extends Command {

    private final EconomyProvider economy;
    private final DiamondEconomy plugin;

    public DepositCommand(EconomyProvider economy, MessageManager messages, DiamondEconomy plugin) {
        super(messages, null, true);
        this.economy = economy;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length != 1) {
            sender.sendMessage(messages.get("deposit.usage"));
            return true;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[0]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[0]));
            return true;
        }

        Material currencyMat = getCurrencyMaterial();
        if (!player.getInventory().contains(currencyMat, (int) amount)) {
            sender.sendMessage(messages.get("errors.not-enough-items"));
            return true;
        }

        player.getInventory().removeItem(new ItemStack(currencyMat, (int) amount));
        economy.depositPlayer(player, amount);

        sender.sendMessage(messages.get("deposit.success", "{amount}", String.valueOf(amount)));

        return true;
    }

    private Material getCurrencyMaterial() {
        String configName = plugin.getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);
        return (material == null) ? Material.DIAMOND : material;
    }
}
