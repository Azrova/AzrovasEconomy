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

        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            return handleHelpCommand(sender, args);
        }

        if (subCommand.equals("admin")) {
            return handleAdminCommand(sender, args);
        }

        if (subCommand.equals("user")) {
            return handleUserCommand(sender, args);
        }

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

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

    private boolean handleHelpCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sendAvailableTopics(sender);
            return true;
        }

        String topic = args[1].toLowerCase();
        switch (topic) {
            case "economy":
                sendEconomyHelp(sender);
                break;
            case "admin":
                sendAdminHelp(sender);
                break;
            case "user":
                sendUserHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown help topic: " + args[1]);
                sendAvailableTopics(sender);
                break;
        }
        return true;
    }

    private void sendAvailableTopics(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Available Help Topics ---");
        sender.sendMessage(ChatColor.YELLOW + "/eco help economy" + ChatColor.GRAY + " - General economy commands");
        sender.sendMessage(ChatColor.YELLOW + "/eco help admin" + ChatColor.GRAY + " - Admin balance commands");
        sender.sendMessage(ChatColor.YELLOW + "/eco help user" + ChatColor.GRAY + " - User management commands");
    }

    private void sendEconomyHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Economy Help ---");
        sender.sendMessage(ChatColor.YELLOW + "/balance (/bal) " + ChatColor.GRAY + "- Check your balance");
        sender.sendMessage(ChatColor.YELLOW + "/pay [player] [amount] " + ChatColor.GRAY + "- Pay another player");
        sender.sendMessage(ChatColor.YELLOW + "/withdraw [amount] " + ChatColor.GRAY + "- Withdraw money as a banknote");
        sender.sendMessage(ChatColor.YELLOW + "/money top " + ChatColor.GRAY + "- See the richest players");
        sender.sendMessage(ChatColor.YELLOW + "/daily " + ChatColor.GRAY + "- Claim your daily reward");
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Admin Balance Commands ---");
        sender.sendMessage(ChatColor.YELLOW + "/eco admin set [player] [amount] " + ChatColor.GRAY + "- Set a player's balance");
        sender.sendMessage(ChatColor.YELLOW + "/eco admin add [player] [amount] " + ChatColor.GRAY + "- Add money to a player");
        sender.sendMessage(ChatColor.YELLOW + "/eco admin remove [player] [amount] " + ChatColor.GRAY + "- Remove money from a player");
        sender.sendMessage(ChatColor.YELLOW + "/eco admin delete [player] " + ChatColor.GRAY + "- Delete a player's economy account");
        sender.sendMessage(ChatColor.GRAY + "Legacy commands:");
        sender.sendMessage(ChatColor.YELLOW + "/eco set [player] [amount] " + ChatColor.GRAY + "- Set a player's balance");
        sender.sendMessage(ChatColor.YELLOW + "/eco remove [player] [amount] " + ChatColor.GRAY + "- Remove money from a player");
        sender.sendMessage(ChatColor.YELLOW + "/eco delete [player] " + ChatColor.GRAY + "- Delete a player's economy account");
    }

    private void sendUserHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- User Management Commands ---");
        sender.sendMessage(ChatColor.YELLOW + "/eco user create [username] " + ChatColor.GRAY + "- Create an economy account for a user");
        sender.sendMessage(ChatColor.YELLOW + "/eco user remove [username] " + ChatColor.GRAY + "- Remove a user's economy account");
        sender.sendMessage(ChatColor.GRAY + "Note: Removed users cannot use any economy commands until recreated");
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco admin [set|add|remove|delete] [player] [amount]");
            return true;
        }

        String adminSubCommand = args[1].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (!plugin.getDatabaseManager().playerExists(target.getUniqueId()) && !adminSubCommand.equals("create")) {
            sender.sendMessage(ChatColor.RED + "Player " + target.getName() + " does not have an economy account.");
            return true;
        }

        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            sender.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return true;
        }

        double amount;

        switch (adminSubCommand) {
            case "set":
                if (!sender.hasPermission("azrova.admin.set")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eco admin set [player] [amount]");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[3]);
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

            case "add":
                if (!sender.hasPermission("azrova.admin.add")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eco admin add [player] [amount]");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be greater than zero.");
                    return true;
                }
                EconomyResponse addResponse = econ.depositPlayer(target, amount);
                if (addResponse.transactionSuccess()) {
                    sender.sendMessage(ChatColor.GREEN + formatCurrency(amount) + " has been added to " + target.getName() + "\'s balance.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not add money: " + addResponse.errorMessage);
                }
                break;

            case "remove":
                if (!sender.hasPermission("azrova.admin.remove")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this sub-command.");
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eco admin remove [player] [amount]");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[3]);
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
                if (args.length != 3) {
                     sender.sendMessage(ChatColor.RED + "Usage: /eco admin delete [player]");
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
                sender.sendMessage(ChatColor.RED + "Usage: /eco admin [set|add|remove|delete] [player] [amount]");
                break;
        }
        return true;
    }

    private boolean handleUserCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco user [create|remove] [username]");
            return true;
        }

        String userSubCommand = args[1].toLowerCase();
        String username = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(username);

        switch (userSubCommand) {
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
                sender.sendMessage(ChatColor.RED + "Usage: /eco user [create|remove] [username]");
                break;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid sub-command. Usage:");
        sender.sendMessage(ChatColor.YELLOW + "/eco help [topic] " + ChatColor.GRAY + "- Show help information");
        sender.sendMessage(ChatColor.YELLOW + "/eco admin [set|add|remove|delete] [player] [amount] " + ChatColor.GRAY + "- Admin balance commands");
        sender.sendMessage(ChatColor.YELLOW + "/eco user [create|remove] [username] " + ChatColor.GRAY + "- User management commands");
        sender.sendMessage(ChatColor.YELLOW + "/eco set [player] [amount] " + ChatColor.GRAY + "- Legacy set command");
        sender.sendMessage(ChatColor.YELLOW + "/eco remove [player] [amount] " + ChatColor.GRAY + "- Legacy remove command");
        sender.sendMessage(ChatColor.YELLOW + "/eco delete [player] " + ChatColor.GRAY + "- Legacy delete command");
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
}
