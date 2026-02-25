package com.mikey1201.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mikey1201.commands.abstracts.Command;
import com.mikey1201.managers.DatabaseManager;
import com.mikey1201.managers.DatabaseManager.BalanceEntry;
import com.mikey1201.managers.HiddenPlayersManager;
import com.mikey1201.managers.MessageManager;
import com.mikey1201.providers.EconomyProvider;

public class BaltopCommand extends Command {

    private final DatabaseManager database;
    private final EconomyProvider economy;
    private final HiddenPlayersManager hiddenPlayersManager;

    public BaltopCommand(DatabaseManager database, EconomyProvider economy, MessageManager messages, HiddenPlayersManager hiddenPlayersManager) {
        super(messages, null, false);
        this.database = database;
        this.economy = economy;
        this.hiddenPlayersManager = hiddenPlayersManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        
        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("diamondeconomy.toggle")) {
                sender.sendMessage(messages.get("errors.no-permission"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("errors.player-only"));
                return true;
            }

            Player player = (Player) sender;
            boolean isNowHidden = hiddenPlayersManager.toggle(player.getUniqueId());
            
            if (isNowHidden) {
                player.sendMessage(messages.get("toggle.hidden"));
            } else {
                player.sendMessage(messages.get("toggle.visible"));
            }
            return true;
        }

        List<BalanceEntry> potentialBalances = database.getTopBalancesWithNames(15);

        if (potentialBalances.isEmpty()) {
            sender.sendMessage(messages.get("baltop.empty"));
            return true;
        }

        sender.sendMessage(messages.get("baltop.header"));
        
        int count = 0;
        for (BalanceEntry entry : potentialBalances) {
            if (count >= 10) break;

            UUID uuid = entry.getUuid();

            if (hiddenPlayersManager.isHidden(uuid)) {
                continue;
            }

            String playerName = entry.getName();
            if (playerName == null || playerName.isEmpty()) {
                playerName = "Unknown";
            }
            
            double balance = entry.getBalance();
            String formattedBalance = economy.format(balance);
            
            sender.sendMessage(messages.get("baltop.entry", 
                "{rank}", String.valueOf(count + 1), 
                "{player}", playerName,
                "{amount}", formattedBalance
            ));
            
            count++;
        }
        
        if (count == 0) {
             sender.sendMessage(messages.get("baltop.empty"));
        }

        return true;
    }
}
