package com.mikey1201.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;
import com.mikey1201.utils.InputUtils;
import com.mikey1201.utils.PlayerUtils;

public class PayCommand extends Command {

    private final EconomyProvider economy;

    public PayCommand(EconomyProvider economy, MessageManager messages) {
        super(messages, null, true);
        this.economy = economy;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length != 2) {
            sender.sendMessage(messages.get("pay.usage"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = PlayerUtils.getOfflinePlayer(targetName);

        if (target == null) {
            sender.sendMessage(messages.get("errors.player-not-found"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(messages.get("errors.pay-self"));
            return true;
        }

        double amount;
        try {
            amount = InputUtils.parsePositiveDouble(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(messages.get("errors.invalid-number", "{input}", args[1]));
            return true;
        }

        if (economy.getBalance(player) < amount) {
            sender.sendMessage(messages.get("errors.insufficient-funds"));
            return true;
        }

        economy.withdrawPlayer(player, amount);
        economy.depositPlayer(target, amount);

        sender.sendMessage(messages.get("pay.success", "{amount}", String.valueOf(amount), "{player}", targetName));

        if (target.isOnline()) {
            ((Player) target).sendMessage(messages.get("pay.received", "{amount}", String.valueOf(amount), "{player}", player.getName()));
        }

        return true;
    }
}
