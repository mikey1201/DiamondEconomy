package com.mikey1201.providers;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mikey1201.managers.DatabaseException;
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
        if (player == null) return false;
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
    public double getBalance(OfflinePlayer player, String world) {
        if (!hasAccount(player)) {
            return 0.0;
        }
        return database.getBalance(player.getUniqueId());
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer p, String w, double a) { return withdrawPlayer(p, a); }
    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) { return withdrawPlayer(s, v); }
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player;
        try {
            player = getRobustOfflinePlayer(playerName);
        } catch (DatabaseException e) {
            return databaseErrorResponse(e);
        }
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player does not exist.");
        }
        if (!isValidAmount(amount)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a non-finite amount.");
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
        }

        try {
            if (!hasAccount(player)) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account doesn't exist.");
            }
            // Atomic funds check + write: two concurrent withdrawals can never
            // both pass and overdraw the account.
            Double newBalance = database.tryWithdraw(player.getUniqueId(), amount);
            if (newBalance == null) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (DatabaseException e) {
            return databaseErrorResponse(e);
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer p, String w, double a) { return depositPlayer(p, a); }
    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) { return depositPlayer(s, v); }
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player;
        try {
            player = getRobustOfflinePlayer(playerName);
        } catch (DatabaseException e) {
            return databaseErrorResponse(e);
        }
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player does not exist.");
        }
        if (!isValidAmount(amount)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a non-finite amount.");
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
        }

        try {
            if (!hasAccount(player)) {
                createPlayerAccount(player);
            }
            // Atomic increment: no read-modify-write race between concurrent deposits.
            double newBalance = database.addToBalance(player.getUniqueId(), playerName(player), amount);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (DatabaseException e) {
            return databaseErrorResponse(e);
        }
    }

    private static EconomyResponse databaseErrorResponse(DatabaseException e) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Database error: " + e.getMessage());
    }

    private static boolean isValidAmount(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    private static String playerName(OfflinePlayer player) {
        // Return the raw name, including null (e.g. Towny town/nation
        // accounts): the database layer preserves any stored name when the
        // provided name is null, instead of overwriting it with "Unknown".
        return player.getName();
    }

    /**
     * Classifies a failed withdrawal for user-facing messaging: if the player
     * can currently afford the amount, the failure must have been a database
     * error rather than insufficient funds (e.g. a lost race).
     */
    public boolean failureWasDatabaseError(OfflinePlayer player, double amount) {
        try {
            return getBalance(player) >= amount;
        } catch (DatabaseException e) {
            return true;
        }
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) { return createPlayerAccount(s); }
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player;
        try {
            player = getRobustOfflinePlayer(playerName);
        } catch (DatabaseException e) {
            return false;
        }
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player == null) return false;
        if (hasAccount(player)) return false;

        try {
            database.createPlayerAccount(player);
            return true;
        } catch (DatabaseException e) {
            return false;
        }
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
        // Locale.ROOT: always produce '1.50', never '1,50' on e.g. German servers
        return String.format(Locale.ROOT, "%.2f", amount);
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
    public List<String> getBanks() { return Collections.emptyList(); }
}
