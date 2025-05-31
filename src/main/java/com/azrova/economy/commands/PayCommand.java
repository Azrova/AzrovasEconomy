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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public PayCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!sender.hasPermission("azrova.economy.pay")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /pay [player] [amount]");
            return true;
        }

        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be greater than zero.");
            return true;
        }

        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            player.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return true;
        }

        EconomyResponse withdrawResponse = econ.withdrawPlayer(player, amount);
        if (withdrawResponse.transactionSuccess()) {
            EconomyResponse depositResponse = econ.depositPlayer(target, amount);
            if (depositResponse.transactionSuccess()) {
                player.sendMessage(ChatColor.GREEN + "You have paid " + target.getName() + " " + formatCurrency(amount, econ) + ".");
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " has paid you " + formatCurrency(amount, econ) + ".");
                }
            } else {
                econ.depositPlayer(player, amount); 
                player.sendMessage(ChatColor.RED + "Could not pay " + target.getName() + ". Error: " + depositResponse.errorMessage);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not have enough money. Error: " + withdrawResponse.errorMessage);
        }
        return true;
    }

    private String formatCurrency(double amount, Economy econ) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 