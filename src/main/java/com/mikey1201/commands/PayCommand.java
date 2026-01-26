package com.mikey1201.commands;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.Messages;
import com.mikey1201.providers.Economy;
import com.mikey1201.utils.InputUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand extends Command {

    private final Economy economy;
    private final Messages messages;

    public PayCommand(Economy economy, Messages messages) {
        super(messages, null, true);
        this.economy = economy;
        this.messages = messages;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
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

        economy.withdrawPlayer(senderPlayer, amount);
        economy.depositPlayer(target, amount);

        senderPlayer.sendMessage(messages.get("pay.sent", "{amount}", String.valueOf(amount), "{player}", target.getName()));
        target.sendMessage(messages.get("pay.received", "{amount}", String.valueOf(amount), "{player}", senderPlayer.getName()));

        return true;
    }
}
