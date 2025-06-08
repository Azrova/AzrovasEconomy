package com.azrova.economy.commands.admin;

import com.azrova.economy.AzrovasEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserAdminCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public UserAdminCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("azrova.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String username = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(username);

        switch (subCommand) {
            case "create":
                if (!sender.hasPermission("eco.admin.user.create")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                
                if (plugin.getDatabaseManager().playerExists(target.getUniqueId())) {
                    sender.sendMessage(ChatColor.YELLOW + "User " + username + " already has an economy account.");
                    return true;
                }
                
                double startingBalance = plugin.getConfig().getDouble("starting-balance", 0.0);
                if (plugin.getDatabaseManager().createPlayerAccount(target.getUniqueId(), startingBalance)) {
                    sender.sendMessage(ChatColor.GREEN + "Economy account created for " + username + " with starting balance of " + formatCurrency(startingBalance) + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to create economy account for " + username + ". Check console for errors.");
                }
                break;

            case "remove":
                if (!sender.hasPermission("eco.admin.user.remove")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                
                if (!plugin.getDatabaseManager().playerExists(target.getUniqueId())) {
                    sender.sendMessage(ChatColor.YELLOW + "User " + username + " does not have an economy account.");
                    return true;
                }
                
                String sql = "DELETE FROM player_economy WHERE uuid = ?";
                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, target.getUniqueId().toString());
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        sender.sendMessage(ChatColor.GREEN + "Economy account removed for " + username + ". They will no longer be able to use economy commands.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to remove economy account for " + username + ".");
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not remove player account from database: " + e.getMessage());
                    sender.sendMessage(ChatColor.RED + "Error removing account. Check console.");
                }
                break;

            default:
                sendUsage(sender);
                break;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage:");
        sender.sendMessage(ChatColor.YELLOW + "/ecouser create [username] - Create an economy account");
        sender.sendMessage(ChatColor.YELLOW + "/ecouser remove [username] - Remove an economy account");
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
}
