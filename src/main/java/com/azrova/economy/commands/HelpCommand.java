package com.azrova.economy.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand implements CommandExecutor {

    private final Map<String, String[]> helpTopics = new HashMap<>();

    public HelpCommand() {
        helpTopics.put("economy", new String[]{
                ChatColor.GOLD + "--- Economy Help ---",
                ChatColor.YELLOW + "/balance (/bal) " + ChatColor.GRAY + "- Check your balance.",
                ChatColor.YELLOW + "/pay [player] [amount] " + ChatColor.GRAY + "- Pay another player.",
                ChatColor.YELLOW + "/withdraw [amount] " + ChatColor.GRAY + "- Withdraw money as a banknote.",
                ChatColor.YELLOW + "/money top " + ChatColor.GRAY + "- See the richest players.",
                ChatColor.YELLOW + "/help economy " + ChatColor.GRAY + "- Shows this help page."
        });
        helpTopics.put("admin", new String[]{
                ChatColor.GOLD + "--- Admin Economy Help ---",
                ChatColor.YELLOW + "/eco set [player] [amount] " + ChatColor.GRAY + "- Set a player's balance.",
                ChatColor.YELLOW + "/eco remove [player] [amount] " + ChatColor.GRAY + "- Remove money from a player.",
                ChatColor.YELLOW + "/eco delete [player] " + ChatColor.GRAY + "- Delete a player's economy account.",
                ChatColor.YELLOW + "/help admin " + ChatColor.GRAY + "- Shows this admin help page."
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("azrova.economy.help")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendAvailableTopics(sender);
            return true;
        }

        String topic = args[0].toLowerCase();
        if (helpTopics.containsKey(topic)) {
            if (topic.equals("admin") && !sender.hasPermission("azrova.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to view admin help.");
                return true;
            }
            for (String line : helpTopics.get(topic)) {
                sender.sendMessage(line);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown help topic: " + args[0]);
            sendAvailableTopics(sender);
        }
        return true;
    }

    private void sendAvailableTopics(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Available Help Topics ---");
        sender.sendMessage(ChatColor.YELLOW + "/help economy" + ChatColor.GRAY + " - General economy commands.");
        if (sender.hasPermission("azrova.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/help admin" + ChatColor.GRAY + " - Admin economy commands.");
        }
        // Add more topics here if needed, e.g., Jobs, AuctionHouse
    }
} 