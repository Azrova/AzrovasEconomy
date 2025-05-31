package com.azrova.economy.commands;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;

    public BalanceCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("azrova.economy.balance")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Economy econ = plugin.getVaultEconomyProvider();
        if (econ == null) {
            sender.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Please specify a player to check their balance.");
                return true;
            }
            Player player = (Player) sender;
            double balance = econ.getBalance(player);
            player.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.GOLD + formatCurrency(balance));
        } else {
            if (!sender.hasPermission("azrova.economy.balance.others")) { 
                sender.sendMessage(ChatColor.RED + "You do not have permission to check other players' balances.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            double balance = econ.getBalance(target);
            sender.sendMessage(ChatColor.GREEN + target.getName() + "\'s balance: " + ChatColor.GOLD + formatCurrency(balance));
        }
        return true;
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 