package com.azrova.economy.commands;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WithdrawCommand implements CommandExecutor {

    private final AzrovasEconomy plugin;
    private final NamespacedKey banknoteKey;

    public WithdrawCommand(AzrovasEconomy plugin) {
        this.plugin = plugin;
        this.banknoteKey = new NamespacedKey(plugin, "banknote_value");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("azrova.economy.withdraw")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /withdraw [amount]");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
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
            ItemStack banknote = new ItemStack(Material.PAPER, 1);
            ItemMeta meta = banknote.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Banknote");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Value: " + formatCurrency(amount, econ));
                lore.add(ChatColor.GRAY + "Right-click to redeem.");
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(banknoteKey, PersistentDataType.DOUBLE, amount);
                banknote.setItemMeta(meta);
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), banknote);
                player.sendMessage(ChatColor.YELLOW + "Your inventory is full. The banknote was dropped on the ground.");
            } else {
                player.getInventory().addItem(banknote);
            }
            player.sendMessage(ChatColor.GREEN + "You have withdrawn " + formatCurrency(amount, econ) + " as a banknote.");

        } else {
            player.sendMessage(ChatColor.RED + "Could not withdraw money: " + withdrawResponse.errorMessage);
        }
        return true;
    }

    private String formatCurrency(double amount, Economy econ) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 