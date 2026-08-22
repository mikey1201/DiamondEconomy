package com.mikey1201.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;

/**
 * SQLite-backed storage for player accounts.
 *
 * All methods throw {@link DatabaseException} on failure instead of silently
 * swallowing errors. Callers must not assume a write succeeded unless the call
 * returns normally; this is critical for an economy plugin where a silently
 * failed withdrawal can duplicate money.
 */
public class DatabaseManager {

    private final String dbPath;
    private Connection connection;
    private final Logger logger;

    /**
     * Simple record class to hold balance entry data for baltop display.
     * Contains UUID, stored name, and balance.
     */
    public static class BalanceEntry {
        private final UUID uuid;
        private final String name;
        private final double balance;

        public BalanceEntry(UUID uuid, String name, double balance) {
            this.uuid = uuid;
            this.name = name;
            this.balance = balance;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public double getBalance() { return balance; }
    }

    public DatabaseManager(File dataFolder, Logger logger) {
        this.dbPath = "jdbc:sqlite:" + new File(dataFolder, "economy.db").getAbsolutePath();
        this.logger = logger;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(dbPath);
            // busy_timeout: wait for the lock instead of failing instantly under
            // concurrent access. WAL: better concurrency and crash safety.
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA busy_timeout = 5000");
                st.execute("PRAGMA journal_mode = WAL");
                st.execute("PRAGMA synchronous = NORMAL");
            }
            logger.info("Successfully connected to the SQLite database.");
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC driver not found!");
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
    }

    /**
     * Returns the live connection, failing fast with a clear error instead of
     * throwing a NullPointerException if the database was never initialized.
     */
    private Connection conn() {
        Connection c = connection;
        if (c == null) {
            throw new DatabaseException("Database is not connected. Economy operations are unavailable.");
        }
        return c;
    }

    public void initialize() throws SQLException {
        try (Statement statement = conn().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY," +
                    "last_known_name TEXT NOT NULL DEFAULT 'Unknown'," +
                    "balance REAL NOT NULL DEFAULT 0.0" +
                    ");");
            logger.info("Database table initialized.");
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.severe("Failed to close database connection: " + e.getMessage());
        }
    }

    public boolean hasAccount(UUID uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DatabaseException("Error checking account", e);
        }
    }

    public void createPlayerAccount(OfflinePlayer player) {
        // Handle null names to prevent constraint violation
        String playerName = player.getName() != null ? player.getName() : "Unknown";

        String sql = "INSERT OR IGNORE INTO players(uuid, last_known_name, balance) VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, playerName);
            pstmt.setDouble(3, 0.0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error creating player account", e);
        }
    }

    public double getBalance(UUID uuid) {
        String sql = "SELECT balance FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting balance", e);
        }
    }

    /**
     * Atomically sets the balance to an absolute value, creating the account
     * if it does not exist yet. Single statement, no read-modify-write race.
     */
    public void setBalance(UUID uuid, double balance) {
        String sql = "INSERT INTO players(uuid, last_known_name, balance) VALUES(?, 'Unknown', ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setDouble(2, balance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error setting balance", e);
        }
    }

    /**
     * Atomically sets the balance to an absolute value and refreshes the stored
     * name (keeping any existing name when {@code name} is null).
     */
    public void updateBalance(UUID uuid, String name, double newBalance) {
        String sql = "INSERT INTO players(uuid, last_known_name, balance) VALUES(?, COALESCE(?, 'Unknown'), ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance, " +
                "last_known_name = COALESCE(excluded.last_known_name, players.last_known_name)";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setDouble(3, newBalance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating balance", e);
        }
    }

    /**
     * Atomically adds {@code delta} to the account's balance, creating the
     * account with {@code delta} as its starting balance if it does not exist.
     * The increment happens inside a single SQL statement, so concurrent calls
     * can never lose an update.
     *
     * @return the balance after the operation
     */
    public double addToBalance(UUID uuid, String name, double delta) {
        String sql = "INSERT INTO players(uuid, last_known_name, balance) VALUES(?, COALESCE(?, 'Unknown'), ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = balance + excluded.balance, " +
                "last_known_name = COALESCE(excluded.last_known_name, players.last_known_name)";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setDouble(3, delta);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error adding to balance", e);
        }
        return getBalance(uuid);
    }

    /**
     * Atomically subtracts {@code amount} from the account's balance, but only
     * if the balance is sufficient. The funds check and the write happen in the
     * same SQL statement, so two concurrent withdrawals can never both succeed
     * and overdraw the account (no check-then-act race).
     *
     * @return the new balance if the withdrawal succeeded, or {@code null} if
     *         the account does not exist or has insufficient funds
     */
    public Double tryWithdraw(UUID uuid, double amount) {
        String sql = "UPDATE players SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
        int rowsUpdated;
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, uuid.toString());
            pstmt.setDouble(3, amount);
            rowsUpdated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error withdrawing balance", e);
        }

        if (rowsUpdated == 0) {
            return null;
        }
        return getBalance(uuid);
    }

    public Map<UUID, Double> getTopBalances(int limit) {
        Map<UUID, Double> topBalances = new LinkedHashMap<>();
        String sql = "SELECT uuid, balance FROM players ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                topBalances.put(uuid, rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting top balances", e);
        }
        return topBalances;
    }

    /**
     * Returns top balances with names from the database.
     * This allows proper display of names for non-player accounts (e.g., Towny towns/nations).
     * @param limit Maximum number of entries to return
     * @return List of BalanceEntry objects containing UUID, name, and balance
     */
    public List<BalanceEntry> getTopBalancesWithNames(int limit) {
        List<BalanceEntry> topBalances = new ArrayList<>();
        String sql = "SELECT uuid, last_known_name, balance FROM players ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("last_known_name");
                double balance = rs.getDouble("balance");
                topBalances.add(new BalanceEntry(uuid, name, balance));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting top balances with names", e);
        }
        return topBalances;
    }

    /**
     * Looks up a UUID by last known player name (case-insensitive).
     *
     * @return the UUID, or {@code null} if no account with that name exists.
     *         Throws {@link DatabaseException} on database failure.
     */
    public UUID getUUID(String playerName) {
        String sql = "SELECT uuid FROM players WHERE last_known_name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("uuid"));
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error looking up player UUID", e);
        }
    }
}