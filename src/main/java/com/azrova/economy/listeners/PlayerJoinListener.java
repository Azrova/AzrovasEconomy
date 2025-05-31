package com.azrova.economy.listeners;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final AzrovasEconomy plugin;

    public PlayerJoinListener(AzrovasEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Economy econ = plugin.getVaultEconomyProvider();

        if (econ == null) {
            plugin.getLogger().warning("Vault economy provider not found, cannot give starting balance.");
            return;
        }

        if (!econ.hasAccount(player)) {
            double startingBalance = plugin.getStartingBalance();
            econ.createPlayerAccount(player);
            econ.depositPlayer(player, startingBalance);
            plugin.getLogger().info(String.format("Created account for %s with starting balance of %s", player.getName(), econ.format(startingBalance)));
        }
    }
} 