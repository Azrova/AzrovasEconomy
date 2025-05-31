package com.azrova.economy.commands;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MoneyCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public MoneyCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("top")) {
            if (!sender.hasPermission("azrova.economy.money.top")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            sendTopBalances(sender);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Usage: /money top");
        return true;
    }

    private void sendTopBalances(CommandSender sender) {
        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            sender.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return;
        }

        List<String> topPlayers = new ArrayList<>();
        String sql = "SELECT uuid, balance FROM player_economy ORDER BY balance DESC LIMIT 10";

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            sender.sendMessage(ChatColor.GOLD + "--- Top Richest Players ---");
            int rank = 1;
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                double balance = rs.getDouble("balance");
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String playerName = (player != null && player.getName() != null) ? player.getName() : "Unknown Player";
                sender.sendMessage(ChatColor.YELLOW + "" + rank + ". " + playerName + " - " + ChatColor.GOLD + formatCurrency(balance));
                rank++;
            }
            if (rank == 1) {
                sender.sendMessage(ChatColor.GRAY + "No players found in the database.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve top balances: " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "Error retrieving top balances. Check console.");
        }
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 