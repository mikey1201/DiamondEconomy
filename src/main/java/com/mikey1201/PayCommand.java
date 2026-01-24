package com.mikey1201;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.text.DecimalFormat;

public class PayCommand implements CommandExecutor {

    private final EconomyProvider economy;
    private final MessageManager messages;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public PayCommand(EconomyProvider economy, MessageManager messages) {
        this.economy = economy;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("errors.player-only"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(messages.get("errors.usage-pay"));
            return true;
        }

        Player senderPlayer = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        if (target.equals(senderPlayer)) {
            sender.sendMessage(messages.get("errors.pay-yourself"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(messages.get("errors.positive-number"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[1]));
            return true;
        }

        if (economy.getBalance(senderPlayer) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        economy.withdrawPlayer(senderPlayer, amount);
        economy.depositPlayer(target, amount);

        String displayAmount = decimalFormat.format(amount);

        senderPlayer.sendMessage(messages.get("pay.sent", "{amount}", displayAmount, "{player}", target.getName()));
        target.sendMessage(messages.get("pay.received", "{amount}", displayAmount, "{player}", senderPlayer.getName()));

        return true;
    }
}