package com.azrova.economy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EconomyPlaceholders extends PlaceholderExpansion {

    private final AzrovasEconomy plugin;

    public EconomyPlaceholders(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "azrovaseconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; 
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("balance")) {
            Economy econ = plugin.getVaultEconomyProvider();
            if (econ != null) {
                return plugin.getCurrencySymbol() + String.format("%.2f", econ.getBalance(player));
            }
            return plugin.getCurrencySymbol() + "0.00"; 
        }
        
        return null; 
    }
} 