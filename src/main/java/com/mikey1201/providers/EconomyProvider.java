package com.mikey1201.providers;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mikey1201.managers.DatabaseManager;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyProvider extends AbstractEconomy {

    private final DatabaseManager database;

    public EconomyProvider(DatabaseManager database) {
        this.database = database;
    }

    private OfflinePlayer getRobustOfflinePlayer(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        UUID uuid = database.getUUID(playerName);
        if (uuid != null) {
            return Bukkit.getOfflinePlayer(uuid);
        }

        return null;
    }

    public boolean hasAccount(String playerName) {
        return database.getUUID(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return false;
        }
        return database.hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String world) {
        return hasAccount(playerName);
    }

    @Override
    public boolean has(String playerName, double amount) { return getBalance(playerName) >= amount; }
    @Override
    public boolean has(OfflinePlayer player, double amount) { return getBalance(player) >= amount; }
    @Override
    public boolean has(String s, String s1, double v) { return has(s, v); }
    @Override
    public double getBalance(String s, String s1) { return getBalance(s); }
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = getRobustOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (!hasAccount(player)) {
            return 0.0;
        }
        return database.getBalance(player.getUniqueId());
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) { return withdrawPlayer(s, v); }
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = getRobustOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account doesn't exist.");
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
        }
        if (getBalance(player) < amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
        }

        double newBalance = getBalance(player) - amount;
        database.updateBalance(player.getUniqueId(), player.getName(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) { return depositPlayer(s, v); }
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = getRobustOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player does not exist.");
        }
        if (!hasAccount(player)) {
            createPlayerAccount(player);
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
        }

        double newBalance = getBalance(player) + amount;
        database.updateBalance(player.getUniqueId(), player.getName(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }
    @Override
    public boolean createPlayerAccount(String s, String s1) { return createPlayerAccount(s); }
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = getRobustOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) return false;
        if (hasAccount(player)) return false;

        database.createPlayerAccount(player);
        return true;
    }

    @Override
    public String getName() { return "DiamondEconomy"; }
    @Override
    public String currencyNamePlural() { return "⬧"; }
    @Override
    public String currencyNameSingular() { return "⬧"; }
    @Override
    public boolean isEnabled() { return true; }
    @Override
    public boolean hasBankSupport() { return false; }
    @Override
    public int fractionalDigits() { return 2; }

    @Override
    public String format(double amount) {
        return String.format("%.2f %s", amount, amount == 1.0 ? currencyNameSingular() : currencyNamePlural());
    }

    @Override
    public EconomyResponse createBank(String n, String p) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse deleteBank(String n) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse bankBalance(String n) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse bankHas(String n, double a) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse bankWithdraw(String n, double a) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse bankDeposit(String n, double a) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse isBankOwner(String n, String p) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public EconomyResponse isBankMember(String n, String p) { return new EconomyResponse(0,0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");}
    @Override
    public List<String> getBanks() { return null; }
}