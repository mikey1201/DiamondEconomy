package com.mikey1201.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

import net.milkbowl.vault.economy.EconomyResponse;

public class PayCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;

    public PayCommand(EconomyProvider economy, MessageManager messages) {
        this.economy = economy;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PlayerUtils.isPlayer(sender, messages.get("errors.player-only"))) return true;
        if (args.length != 2) {
            sender.sendMessage(messages.get("errors.usage-pay"));
            return true;
        }

        Player senderPlayer = (Player) sender;
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        if (target.equals(senderPlayer)) {
            sender.sendMessage(messages.get("errors.pay-yourself"));
            return true;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[1]));
            return true;
        }

        if (economy.getBalance(senderPlayer) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        // FIX: check responses and compensate if either half of the transfer
        // fails, instead of always reporting success.
        EconomyResponse withdrawResponse = economy.withdrawPlayer(senderPlayer, amount);
        if (withdrawResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            if (economy.failureWasDatabaseError(senderPlayer, amount)) {
                Bukkit.getLogger().warning("DiamondEconomy: /pay withdraw failed for " + senderPlayer.getName() + ": " + withdrawResponse.errorMessage);
                senderPlayer.sendMessage(messages.get("errors.unknown-error"));
            } else {
                senderPlayer.sendMessage(messages.get("errors.insufficient-funds"));
            }
            return true;
        }

        EconomyResponse depositResponse = economy.depositPlayer(target, amount);
        if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            // The withdrawal succeeded but the deposit did not: refund the
            // sender so money cannot vanish into a failed deposit.
            Bukkit.getLogger().warning("DiamondEconomy: /pay deposit failed for " + target.getName() + " (refunding " + senderPlayer.getName() + "): " + depositResponse.errorMessage);
            EconomyResponse refundResponse = economy.depositPlayer(senderPlayer, amount);
            if (refundResponse.type != EconomyResponse.ResponseType.SUCCESS) {
                Bukkit.getLogger().severe("DiamondEconomy: CRITICAL - refund failed for " + senderPlayer.getName() + ", amount " + amount + ". Manual intervention required.");
            }
            senderPlayer.sendMessage(messages.get("errors.unknown-error"));
            return true;
        }

        senderPlayer.sendMessage(messages.get("pay.sent", "{amount}", String.valueOf(amount), "{player}", target.getName()));
        target.sendMessage(messages.get("pay.received", "{amount}", String.valueOf(amount), "{player}", senderPlayer.getName()));

        return true;
    }
}
