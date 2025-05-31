package com.azrova.economy;

import com.azrova.economy.commands.*;
import com.azrova.economy.listeners.BanknoteListener;
import com.azrova.economy.listeners.PlayerJoinListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class AzrovasEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private double startingBalance;
    private double dailyRewardAmount;
    private String currencySymbol;
    private AzrovaVaultEconomy vaultEconomyProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.startingBalance = getConfig().getDouble("starting-balance", 100.0);
        this.dailyRewardAmount = getConfig().getDouble("daily-reward-amount", 100.0);
        this.currencySymbol = getConfig().getString("currency-symbol", "$");

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugin not found! Azrova's Economy requires Vault to function.");
            getLogger().severe("Please install Vault and restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.databaseManager = new DatabaseManager(this);
        databaseManager.initializeDatabase();

        this.vaultEconomyProvider = new AzrovaVaultEconomy(this);
        getServer().getServicesManager().register(Economy.class, this.vaultEconomyProvider, this, ServicePriority.Normal);
        getLogger().info("Registered AzrovaVaultEconomy as the economy provider with Vault.");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EconomyPlaceholders(this).register();
            getLogger().info("PlaceholderAPI found, placeholders registered.");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders will not be registered. This is an optional dependency.");
        }

        registerCommands();
        registerListeners();

        getLogger().info("-------------------------------------");
        getLogger().info("Azrova's Economy");
        getLogger().info("Made with ❤ by CNethuka and Sobble");
        getLogger().info("For updates and reports, visit https://github.com/Azrova/AzrovasEconomy");
        getLogger().info("-------------------------------------");
        getLogger().info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(this));
        Objects.requireNonNull(getCommand("withdraw")).setExecutor(new WithdrawCommand(this));
        Objects.requireNonNull(getCommand("help")).setExecutor(new HelpCommand());
        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand(this));
        Objects.requireNonNull(getCommand("money")).setExecutor(new MoneyCommand(this));
        Objects.requireNonNull(getCommand("eco")).setExecutor(new EcoCommand(this));
        Objects.requireNonNull(getCommand("daily")).setExecutor(new DailyCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BanknoteListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public double getStartingBalance() {
        return startingBalance;
    }

    public double getDailyRewardAmount() {
        return dailyRewardAmount;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    public Economy getVaultEconomyProvider() {
        return vaultEconomyProvider;
    }
} 