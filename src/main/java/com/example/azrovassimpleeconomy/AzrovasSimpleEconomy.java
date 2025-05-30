package com.example.azrovassimpleeconomy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.milkbowl.vault.economy.Economy;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class AzrovasSimpleEconomy extends JavaPlugin implements Listener {

    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<UUID, JobType> playerJobs = new ConcurrentHashMap<>();
    private final Map<UUID, Long> dailyCooldowns = new ConcurrentHashMap<>();

    private FileConfiguration appConfig;
    private String currencySymbol;
    private String ecoCommandBaseAlias;
    private double startingBalance;
    private double dailyRewardAmount;
    private long dailyRewardCooldownSeconds;

    private static AzrovasSimpleEconomy instance;

    @Override
    public void onEnable() {
        instance = this;
        printStartupMessages();
        loadAppConfig();

        if (!setupEconomyProvider()) {
            getLogger().severe(String.format("[%s] - Failed to initialize or register with Vault/VaultUnlocked. Plugin will be disabled.", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new JobListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EconomyPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found, placeholders registered.");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders will not be available.");
        }

        getLogger().info(ChatColor.GREEN + "Azrova's Simple Economy has been enabled and registered as an economy provider!");

        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));
        getCommand("jobs").setExecutor(new JobsCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "Azrova's Simple Economy has been disabled!");
    }

    private void printStartupMessages() {
        getLogger().info(ChatColor.AQUA + "------------------------------------------");
        getLogger().info(ChatColor.GOLD + "      Azrova's Simple Economy");
        getLogger().info("");
        getLogger().info(ChatColor.YELLOW + "        Made by CNethuka");
        getLogger().info(ChatColor.RED + "        Made with \u2764");
        getLogger().info("");
        getLogger().info(ChatColor.GREEN + "  For latest updates, check out:");
        getLogger().info(ChatColor.BLUE + "  https://github.com/azrova/AzrovasEconomy");
        getLogger().info(ChatColor.AQUA + "------------------------------------------");
    }

    public FileConfiguration getAppConfig() {
        return appConfig;
    }

    private void loadAppConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        appConfig = getConfig();
        appConfig.options().copyDefaults(true);
        saveConfig();

        currencySymbol = appConfig.getString("options.currency-symbol", "$");
        ecoCommandBaseAlias = appConfig.getString("options.eco-command-alias", "eco");
        startingBalance = appConfig.getDouble("options.starting-balance", 100.0);
        dailyRewardAmount = appConfig.getDouble("daily-reward.amount", 100.0);
        dailyRewardCooldownSeconds = appConfig.getLong("daily-reward.cooldown", 86400);

        if (getCommand(ecoCommandBaseAlias) == null && !ecoCommandBaseAlias.equalsIgnoreCase("eco")) {
            getLogger().warning("The custom eco command alias '" + ecoCommandBaseAlias + "' from config.yml might not be fully functional if not also defined as an alias in plugin.yml.");
        }
    }

    private boolean setupEconomyProvider() {
        if (getServer().getPluginManager().getPlugin("Vault") == null && getServer().getPluginManager().getPlugin("VaultUnlocked") == null) {
            getLogger().warning("Neither Vault nor VaultUnlocked found! Azrova's Simple Economy requires one of them to function as an economy provider.");
            return false;
        }
        getServer().getServicesManager().register(Economy.class, new Economy_Azrova(this), this, ServicePriority.Normal);
        getLogger().info("Successfully registered Azrova's Simple Economy as an Economy provider with Vault/VaultUnlocked.");
        return true;
    }

    public static AzrovasSimpleEconomy getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!balances.containsKey(playerUUID)) {
            if (!player.hasPlayedBefore()) {
                 setBalance(playerUUID, startingBalance);
                 player.sendMessage(ChatColor.GREEN + String.format("Welcome! You have started with %s.", format(startingBalance)));
            } else {
                setBalance(playerUUID, startingBalance);
                player.sendMessage(ChatColor.YELLOW + String.format("Your %s account has been initialized with %s.", getName(), format(startingBalance)));
            }
        }
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getEcoCommandBaseAlias() {
        return ecoCommandBaseAlias;
    }

    public Map<UUID, Double> getBalances() {
        return balances;
    }

    public double getBalance(UUID playerUUID) {
        return balances.getOrDefault(playerUUID, 0.0);
    }

    public void setBalance(UUID playerUUID, double amount) {
        if (amount < 0) amount = 0;
        balances.put(playerUUID, amount);
    }

    public void deposit(UUID playerUUID, double amount) {
        if (amount <= 0) return;
        setBalance(playerUUID, getBalance(playerUUID) + amount);
    }

    public boolean withdraw(UUID playerUUID, double amount) {
        if (amount <= 0) return false;
        if (getBalance(playerUUID) >= amount) {
            setBalance(playerUUID, getBalance(playerUUID) - amount);
            return true;
        }
        return false;
    }

    public String format(double amount) {
        return String.format("%s%.2f", currencySymbol, amount);
    }

    public JobType getPlayerJob(Player player) {
        return playerJobs.get(player.getUniqueId());
    }

    public void setPlayerJob(Player player, JobType jobType) {
        if (jobType == null) {
            playerJobs.remove(player.getUniqueId());
        } else {
            playerJobs.put(player.getUniqueId(), jobType);
        }
    }

    public double getDailyRewardAmount() {
        return dailyRewardAmount;
    }

    public long getDailyRewardCooldownSeconds() {
        return dailyRewardCooldownSeconds;
    }

    public Map<UUID, Long> getDailyCooldowns() {
        return dailyCooldowns;
    }

    public void processJobAction(Player player, JobType jobType, String actionKey, double defaultAmount) {
        if (getPlayerJob(player) != jobType) {
            return;
        }
        double earnings = getAppConfig().getDouble("jobs.rewards." + jobType.name() + "." + actionKey, defaultAmount);
        if (earnings > 0) {
            deposit(player.getUniqueId(), earnings);
        }
    }
} 