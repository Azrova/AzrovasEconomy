package com.azrova.economy.listeners;

import com.azrova.economy.AzrovasEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BanknoteListener implements Listener {

    private final AzrovasEconomy plugin;
    private final NamespacedKey banknoteKey;

    public BanknoteListener(AzrovasEconomy plugin) {
        this.plugin = plugin;
        this.banknoteKey = new NamespacedKey(plugin, "banknote_value");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PAPER) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(banknoteKey, PersistentDataType.DOUBLE)) {
                event.setCancelled(true);
                Double value = meta.getPersistentDataContainer().get(banknoteKey, PersistentDataType.DOUBLE);
                if (value == null || value <= 0) {
                    player.sendMessage(ChatColor.RED + "This banknote is invalid or has no value.");
                    return;
                }

                Economy econ = plugin.getVaultEconomyProvider();
                if (econ == null) {
                    player.sendMessage(ChatColor.RED + "Economy system not properly initialized. Please contact an administrator.");
                    return;
                }

                EconomyResponse depositResponse = econ.depositPlayer(player, value);
                if (depositResponse.transactionSuccess()) {
                    player.sendMessage(ChatColor.GREEN + "You have redeemed a banknote worth " + formatCurrency(value) + ".");
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItem(event.getHand(), null);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Could not redeem banknote: " + depositResponse.errorMessage);
                }
            }
        }
    }

    private String formatCurrency(double amount) {
        return plugin.getCurrencySymbol() + String.format("%.2f", amount);
    }
} 