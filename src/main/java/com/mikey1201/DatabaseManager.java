package com.mikey1201;

import org.bukkit.OfflinePlayer;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseManager {

    private final String dbPath;
    private Connection connection;
    private final Logger logger;

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
            logger.info("Successfully connected to the SQLite database.");
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC driver not found!");
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
    }

    public void initialize() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY," +
                    "last_known_name TEXT NOT NULL," +
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.severe("Error checking account: " + e.getMessage());
            return false;
        }
    }

    public void createPlayerAccount(OfflinePlayer player) {
        String sql = "INSERT OR IGNORE INTO players(uuid, last_known_name, balance) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName() != null ? player.getName() : "Unknown");
            pstmt.setDouble(3, 0.0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error creating player account: " + e.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        String sql = "SELECT balance FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            logger.severe("Error getting balance: " + e.getMessage());
        }
        return 0.0;
    }

    public void updateBalance(UUID uuid, String name, double newBalance) {
        String sql = "INSERT INTO players(uuid, last_known_name, balance) VALUES(?,?,?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance, last_known_name = excluded.last_known_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setDouble(3, newBalance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating balance: " + e.getMessage());
        }
    }

    public Map<String, Double> getTopBalances(int limit) {
        Map<String, Double> topBalances = new LinkedHashMap<>();
        String sql = "SELECT last_known_name, balance FROM players ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                topBalances.put(rs.getString("last_known_name"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            logger.severe("Error getting top balances: " + e.getMessage());
        }
        return topBalances;
    }
}