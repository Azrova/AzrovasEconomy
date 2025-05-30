package com.example.azrovassimpleeconomy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EconomyPlaceholder extends PlaceholderExpansion {

    private final AzrovasSimpleEconomy plugin;

    public EconomyPlaceholder(AzrovasSimpleEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "azeco";
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
            return plugin.format(plugin.getBalance(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("balance_formatted")) {
            return plugin.format(plugin.getBalance(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("balance_raw")) {
            return String.valueOf(plugin.getBalance(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("currency_symbol")) {
            return plugin.getCurrencySymbol();
        }

        return null;
    }
} 