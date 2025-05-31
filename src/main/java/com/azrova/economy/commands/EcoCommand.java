package com.azrova.economy.commands;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
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

public class EcoCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public EcoCommand(AzrovasEconomy plugin) {
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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            sender.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return true;
        }

        double amount;

        switch (subCommand) {
            case "set":
                if (!sender.hasPermission("azrova.admin.set")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eco set [player] [amount]");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }
                if (amount < 0) {
                    sender.sendMessage(ChatColor.RED + "Amount cannot be negative.");
                    return true;
                }

                double currentBalanceSet = econ.getBalance(target);
                econ.withdrawPlayer(target, currentBalanceSet); 
                EconomyResponse setResponse = econ.depositPlayer(target, amount);
                if (setResponse.transactionSuccess()) {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + "\'s balance has been set to " + formatCurrency(amount) + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not set balance: " + setResponse.errorMessage);
                }
                break;

            case "remove":
                if (!sender.hasPermission("azrova.admin.remove")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eco remove [player] [amount]");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be greater than zero.");
                    return true;
                }
                EconomyResponse removeResponse = econ.withdrawPlayer(target, amount);
                if (removeResponse.transactionSuccess()) {
                    sender.sendMessage(ChatColor.GREEN + formatCurrency(amount) + " has been removed from " + target.getName() + "\'s balance.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not remove money: " + removeResponse.errorMessage);
                }
                break;

            case "delete":
                if (!sender.hasPermission("azrova.admin.delete")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Usage: /eco delete [player]");
                     return true;
                }
                
                double currentBalanceDelete = econ.getBalance(target);
                EconomyResponse deleteResponse = econ.withdrawPlayer(target, currentBalanceDelete);

                if (deleteResponse.transactionSuccess()) {
                     String sql = "DELETE FROM player_economy WHERE uuid = ?";
                     try (Connection conn = plugin.getDatabaseManager().getConnection();
                          PreparedStatement pstmt = conn.prepareStatement(sql)) {
                         pstmt.setString(1, target.getUniqueId().toString());
                         int affectedRows = pstmt.executeUpdate();
                         if (affectedRows > 0) {
                            sender.sendMessage(ChatColor.GREEN + target.getName() + "'s economy account has been deleted.");
                         } else {
                            sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s economy account was not found in the database, but their Vault balance (if any) was cleared.");
                         }
                     } catch (SQLException e) {
                         plugin.getLogger().severe("Could not delete player account from database: " + e.getMessage());
                         sender.sendMessage(ChatColor.RED + "Error deleting account. Check console.");
                     }
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not clear balance before deleting account: " + deleteResponse.errorMessage);
                }
                break;

            default:
                sendUsage(sender);
                break;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid sub-command. Usage:");
        sender.sendMessage(ChatColor.YELLOW + "/eco set [player] [amount]");
        sender.sendMessage(ChatColor.YELLOW + "/eco remove [player] [amount]");
        sender.sendMessage(ChatColor.YELLOW + "/eco delete [player]");
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 