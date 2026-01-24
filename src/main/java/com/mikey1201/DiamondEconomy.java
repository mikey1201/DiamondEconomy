package com.mikey1201;

import java.sql.SQLException;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Material;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiamondEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyProvider economyProvider;
    private MessageManager messageManager;
    private HiddenPlayersManager hiddenPlayersManager;

    private static final int BSTATS_ID = 29024;


    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        databaseManager = new DatabaseManager(getDataFolder(), getLogger());
        try {
            databaseManager.connect();
            databaseManager.initialize();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize the database! The plugin may not function correctly.");
            e.printStackTrace();
            return;
        }

        economyProvider = new EconomyProvider(databaseManager);
        VaultHook.hook(this, databaseManager, getLogger());
    }

    @Override
    public void onEnable() {
        if (databaseManager == null || economyProvider == null) {
            getLogger().severe("Critical components failed to load in onLoad. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        
        messageManager = new MessageManager(this);
        hiddenPlayersManager = new HiddenPlayersManager(this);

        registerCommands();

        if (getConfig().getBoolean("bstats-enabled", true)) {
            Metrics metrics = new Metrics(this, BSTATS_ID);
            addBStatsCharts(metrics);
            getLogger().info("bStats metrics enabled. Thank you for helping!");
        } else {
            getLogger().info("bStats metrics disabled via config.yml.");
        }

        new UpdateChecker(this).checkForUpdates();

        getServer().getPluginManager().registerEvents(new PlayerAccountListener(databaseManager), this);

        getLogger().info("DiamondEconomy has been enabled!");
    }

    private void addBStatsCharts(Metrics metrics) {
        metrics.addCustomChart(new SimplePie("used_item", () -> {
            return getCurrencyMaterial().toString();
        }));
    }

    private Material getCurrencyMaterial() {
        String configName = getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);

        if (material == null || !material.isItem()) {
            getLogger().warning("Invalid material '" + configName + "' in config.yml. Defaulting to DIAMOND.");
            return Material.DIAMOND;
        }
        return material;
    }

    private void registerCommands() {
        TabCompleter tabCompleter = new CommandTabCompleter();

        this.getCommand("balance").setExecutor(new BalanceCommand(economyProvider, messageManager));
        this.getCommand("balance").setTabCompleter(tabCompleter);

        this.getCommand("deposit").setExecutor(new DepositCommand(economyProvider, messageManager, this));
        this.getCommand("deposit").setTabCompleter(tabCompleter);

        this.getCommand("withdraw").setExecutor(new WithdrawCommand(economyProvider, messageManager, this));
        this.getCommand("withdraw").setTabCompleter(tabCompleter);

        this.getCommand("pay").setExecutor(new PayCommand(economyProvider, messageManager));
        this.getCommand("pay").setTabCompleter(tabCompleter);

        this.getCommand("baltop").setExecutor(new BaltopCommand(databaseManager, economyProvider, messageManager, hiddenPlayersManager));
        this.getCommand("baltop").setTabCompleter(tabCompleter);

        this.getCommand("eco").setExecutor(new EcoCommand(economyProvider, messageManager, databaseManager));
        this.getCommand("diamondeconomy").setExecutor(new DiamondEconomyCommand(this, messageManager));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("DiamondEconomy has been disabled.");
    }
}