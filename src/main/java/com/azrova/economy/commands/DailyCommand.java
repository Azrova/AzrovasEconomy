package com.azrova.economy.commands;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DailyCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public DailyCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("azrova.economy.daily")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            player.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return true;
        }

        long lastClaimTime = getLastClaimTime(player);
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);

        if (currentTime - lastClaimTime < twentyFourHoursInMillis) {
            long timeRemaining = twentyFourHoursInMillis - (currentTime - lastClaimTime);
            String remaining = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timeRemaining),
                    TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % TimeUnit.MINUTES.toSeconds(1));
            player.sendMessage(ChatColor.RED + "You have already claimed your daily reward. Time remaining: " + remaining);
            return true;
        }

        double rewardAmount = plugin.getDailyRewardAmount();
        EconomyResponse response = econ.depositPlayer(player, rewardAmount);

        if (response.transactionSuccess()) {
            updateLastClaimTime(player, currentTime);
            player.sendMessage(ChatColor.GREEN + "You have claimed your daily reward of " + formatCurrency(rewardAmount) + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Could not claim daily reward: " + response.errorMessage);
        }
        return true;
    }

    private long getLastClaimTime(Player player) {
        String sql = "SELECT last_daily_claim FROM player_economy WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("last_daily_claim");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get last daily claim time: " + e.getMessage());
        }
        return 0; 
    }

    private void updateLastClaimTime(Player player, long time) {
        String sql = "UPDATE player_economy SET last_daily_claim = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, time);
            pstmt.setString(2, player.getUniqueId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update last daily claim time: " + e.getMessage());
        }
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 