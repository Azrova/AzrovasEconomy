package com.azrova.economy;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private final AzrovasEconomy plugin;

    public DatabaseManager(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    public void initializeDatabase() {
        File dataFolder = new File(plugin.getDataFolder(), "economy.db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.getParentFile().mkdirs();
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create database file!" + e.getMessage());
                return;
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
            plugin.getLogger().info("SQLite database connected.");
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database! " + e.getMessage());
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found! " + e.getMessage());
        }
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_economy (" +
                     "uuid TEXT PRIMARY KEY NOT NULL, " +
                     "balance REAL NOT NULL DEFAULT 0.0, " +
                     "last_daily_claim INTEGER DEFAULT 0" +
                     ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Player economy table created or already exists.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create player_economy table! " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                File dataFolder = new File(plugin.getDataFolder(), "economy.db");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not reconnect to SQLite database! " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close SQLite connection! " + e.getMessage());
        }
    }

    public boolean playerExists(UUID uuid) {
        String sql = "SELECT 1 FROM player_economy WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player exists: " + e.getMessage());
            return false;
        }
    }

    public double getPlayerBalance(UUID uuid) {
        String sql = "SELECT balance FROM player_economy WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get player balance: " + e.getMessage());
        }
        return 0.0; // Default to 0 if player not found or error
    }

    public boolean updateBalance(UUID uuid, double newBalance) {
        String sql = "UPDATE player_economy SET balance = ? WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, uuid.toString());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update player balance: " + e.getMessage());
            return false;
        }
    }

    public boolean createPlayerAccount(UUID uuid, double startingBalance) {
        if (playerExists(uuid)) {
            return true; // Account already exists
        }
        String sql = "INSERT INTO player_economy(uuid, balance, last_daily_claim) VALUES(?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setDouble(2, startingBalance);
            pstmt.setLong(3, 0); // Initialize last_daily_claim
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create player account: " + e.getMessage());
            return false;
        }
    }
} 