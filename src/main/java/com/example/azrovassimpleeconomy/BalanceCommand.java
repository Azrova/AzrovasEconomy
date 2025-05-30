package com.example.azrovassimpleeconomy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final AzrovasSimpleEconomy plugin;

    public BalanceCommand(AzrovasSimpleEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("azrovaseconomy.balance")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        double balance = plugin.getBalance(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Your balance is: " + plugin.format(balance));
        return true;
    }
} 