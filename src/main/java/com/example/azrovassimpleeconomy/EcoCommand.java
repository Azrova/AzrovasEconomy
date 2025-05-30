package com.example.azrovassimpleeconomy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EcoCommand implements CommandExecutor, TabCompleter {

    private final AzrovasSimpleEconomy plugin;
    private List<Map.Entry<UUID, Double>> topBalancesCache = new ArrayList<>();
    private long lastCacheUpdateTime = 0;

    public EcoCommand(AzrovasSimpleEconomy plugin) {
        this.plugin = plugin;
        updateTopBalancesCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("azrovaseconomy.access")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "pay":
                handlePay(sender, args);
                break;
            case "add":
                handleAdd(sender, args);
                break;
            case "withdraw":
            case "remove":
            case "take":
                handleWithdraw(sender, args);
                break;
            case "removeaccount":
                handleRemoveAccount(sender, args);
                break;
            case "top":
                handleTop(sender);
                break;
            case "daily":
                handleDaily(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown sub-command. Use /" + plugin.getEcoCommandBaseAlias() + " help for assistance.");
                break;
        }
        return true;
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("azrovaseconomy.pay")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /" + plugin.getEcoCommandBaseAlias() + " pay [player] [amount]");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid amount.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "The amount must be positive.");
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot pay yourself!");
            return;
        }

        if (plugin.withdraw(player.getUniqueId(), amount)) {
            plugin.deposit(target.getUniqueId(), amount);
            player.sendMessage(ChatColor.GREEN + "You paid " + target.getName() + " " + plugin.format(amount) + ".");
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " paid you " + plugin.format(amount) + ".");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not have enough money. Your balance: " + plugin.format(plugin.getBalance(player.getUniqueId())));
        }
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azrovaseconomy.admin.add")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + plugin.getEcoCommandBaseAlias() + " add [player] [amount]");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid amount.");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive.");
            return;
        }
        plugin.deposit(target.getUniqueId(), amount);
        sender.sendMessage(ChatColor.GREEN + "You added " + plugin.format(amount) + " to " + target.getName() + "'s balance.");
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GREEN + "Your balance was increased by " + plugin.format(amount) + " by " + sender.getName() + ".");
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azrovaseconomy.admin.withdraw")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + plugin.getEcoCommandBaseAlias() + " withdraw [player] [amount]");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid amount.");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive.");
            return;
        }
        if (plugin.withdraw(target.getUniqueId(), amount)) {
            sender.sendMessage(ChatColor.GREEN + "You withdrew " + plugin.format(amount) + " from " + target.getName() + "'s balance.");
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(ChatColor.RED + plugin.format(amount) + " was withdrawn from your balance by " + sender.getName() + ".");
            }
        } else {
            sender.sendMessage(ChatColor.RED + target.getName() + " does not have enough money. Their balance: " + plugin.format(plugin.getBalance(target.getUniqueId())));
        }
    }

    private void handleRemoveAccount(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azrovaseconomy.admin.remove")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + plugin.getEcoCommandBaseAlias() + " removeaccount [player]");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline() && !plugin.getBalances().containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found or has no account.");
            return;
        }

        if (plugin.getBalances().remove(target.getUniqueId()) != null) {
            sender.sendMessage(ChatColor.GREEN + "Account for " + target.getName() + " has been removed.");
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(ChatColor.RED + "Your economy account has been removed by an administrator.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Could not remove account for " + target.getName() + ". They might not have an account.");
        }
    }

    private void handleDaily(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("azrovaseconomy.daily")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000;
        long lastClaimed = plugin.getDailyCooldowns().getOrDefault(playerUUID, 0L);
        long cooldown = plugin.getDailyRewardCooldownSeconds();

        if (currentTime - lastClaimed >= cooldown) {
            double amount = plugin.getDailyRewardAmount();
            plugin.deposit(playerUUID, amount);
            plugin.getDailyCooldowns().put(playerUUID, currentTime);
            player.sendMessage(ChatColor.GREEN + "You have claimed your daily reward of " + plugin.format(amount) + "!");
        } else {
            long timeLeft = cooldown - (currentTime - lastClaimed);
            long hours = timeLeft / 3600;
            long minutes = (timeLeft % 3600) / 60;
            long seconds = timeLeft % 60;
            player.sendMessage(ChatColor.RED + String.format("You must wait %02d:%02d:%02d before claiming your daily reward again.", hours, minutes, seconds));
        }
    }

    private void updateTopBalancesCache() {
        long now = System.currentTimeMillis();
        long updateIntervalMillis = plugin.getAppConfig().getLong("options.eco-top.update-interval", 300) * 1000;
        if (now - lastCacheUpdateTime > updateIntervalMillis || topBalancesCache.isEmpty()) {
            topBalancesCache = plugin.getBalances().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
            lastCacheUpdateTime = now;
        }
    }

    private void handleTop(CommandSender sender) {
        if (!sender.hasPermission("azrovaseconomy.top")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        updateTopBalancesCache();
        sender.sendMessage(ChatColor.GOLD + "--- Top Balances ---");
        int listLength = plugin.getAppConfig().getInt("options.eco-top.list-length", 10);
        for (int i = 0; i < Math.min(listLength, topBalancesCache.size()); i++) {
            Map.Entry<UUID, Double> entry = topBalancesCache.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + playerName + ChatColor.GRAY + " - " + ChatColor.WHITE + plugin.format(entry.getValue()));
        }
        if (topBalancesCache.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No balances recorded yet.");
        }
    }

    private void sendHelp(CommandSender sender) {
        String ecoCmd = plugin.getEcoCommandBaseAlias();
        sender.sendMessage(ChatColor.GOLD + "--- Azrova's Simple Economy Help ---");
        if (sender.hasPermission("azrovaseconomy.pay")) {
            sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " pay [player] [amount]" + ChatColor.GRAY + " - Pay another player.");
        }
        if (sender.hasPermission("azrovaseconomy.balance")) {
             sender.sendMessage(ChatColor.YELLOW + "/balance" + ChatColor.GRAY + " or " + ChatColor.YELLOW + "/bal" + ChatColor.GRAY + " - Check your balance.");
        }
        if (sender.hasPermission("azrovaseconomy.top")) {
            sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " top" + ChatColor.GRAY + " - View the richest players.");
        }
        if (sender.hasPermission("azrovaseconomy.daily")) {
            sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " daily" + ChatColor.GRAY + " - Claim your daily reward.");
        }
        if (sender.hasPermission("azrovaseconomy.help")) {
            sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " help" + ChatColor.GRAY + " - Shows this help message.");
        }

        if (sender.hasPermission("azrovaseconomy.admin.help") || sender.hasPermission("azrovaseconomy.admin.add") || sender.hasPermission("azrovaseconomy.admin.withdraw") || sender.hasPermission("azrovaseconomy.admin.remove")) {
            sender.sendMessage(ChatColor.GOLD + "--- Admin Commands ---");
            if (sender.hasPermission("azrovaseconomy.admin.add")) {
                sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " add [player] [amount]" + ChatColor.GRAY + " - Add money to a player's balance.");
            }
            if (sender.hasPermission("azrovaseconomy.admin.withdraw")) {
                sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " withdraw [player] [amount]" + ChatColor.GRAY + " - Remove money from a player's balance.");
            }
            if (sender.hasPermission("azrovaseconomy.admin.remove")) {
                sender.sendMessage(ChatColor.YELLOW + "/" + ecoCmd + " removeaccount [player]" + ChatColor.GRAY + " - Deletes a player's economy account.");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("eco")) {
            if (args.length == 1) {
                List<String> subcommands = new ArrayList<>();
                if (sender.hasPermission("azrovaseconomy.pay")) subcommands.add("pay");
                if (sender.hasPermission("azrovaseconomy.top")) subcommands.add("top");
                if (sender.hasPermission("azrovaseconomy.daily")) subcommands.add("daily");
                if (sender.hasPermission("azrovaseconomy.help")) subcommands.add("help");
                if (sender.hasPermission("azrovaseconomy.admin.add")) subcommands.add("add");
                if (sender.hasPermission("azrovaseconomy.admin.withdraw")) {
                    subcommands.add("withdraw");
                    subcommands.add("remove");
                    subcommands.add("take");
                }
                if (sender.hasPermission("azrovaseconomy.admin.remove")) subcommands.add("removeaccount");

                return subcommands.stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 2) {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("pay") || subCommand.equals("add") || subCommand.equals("withdraw") || subCommand.equals("remove") || subCommand.equals("take") || subCommand.equals("removeaccount")) {
                    return null;
                }
            }
        }
        return Collections.emptyList();
    }
} 