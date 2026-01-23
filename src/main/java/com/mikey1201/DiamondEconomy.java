package com.mikey1201;

import org.bukkit.Material;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;

public final class DiamondEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyProvider economyProvider;

    @Override
    public void onLoad() {
        // ... (Keep existing onLoad code) ...
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

        // FIX APPLIED: Save default config and load currency item
        saveDefaultConfig();
        Material currencyItem = getCurrencyMaterial();

        registerCommands(currencyItem);

        getServer().getPluginManager().registerEvents(new PlayerAccountListener(databaseManager), this);

        getLogger().info("DiamondEconomy has been enabled!");
    }

    private Material getCurrencyMaterial() {
        String configName = getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);

        if (material == null) {
            getLogger().warning("Invalid material '" + configName + "' in config.yml. Defaulting to DIAMOND.");
            return Material.DIAMOND;
        }
        if (!material.isItem()) {
            getLogger().warning("Material '" + configName + "' is not a valid item. Defaulting to DIAMOND.");
            return Material.DIAMOND;
        }
        
        getLogger().info("Currency item set to: " + material.name());
        return material;
    }

    // UPDATED: Now accepts Material argument
    private void registerCommands(Material currencyItem) {
        TabCompleter tabCompleter = new CommandTabCompleter();

        this.getCommand("balance").setExecutor(new BalanceCommand(economyProvider));
        this.getCommand("balance").setTabCompleter(tabCompleter);

        // FIX: Pass currencyItem here
        this.getCommand("deposit").setExecutor(new DepositCommand(economyProvider, currencyItem));
        this.getCommand("deposit").setTabCompleter(tabCompleter);

        // FIX: Pass currencyItem here
        this.getCommand("withdraw").setExecutor(new WithdrawCommand(economyProvider, currencyItem));
        this.getCommand("withdraw").setTabCompleter(tabCompleter);

        this.getCommand("pay").setExecutor(new PayCommand(economyProvider));
        this.getCommand("pay").setTabCompleter(tabCompleter);

        this.getCommand("baltop").setExecutor(new BaltopCommand(databaseManager, economyProvider));
        this.getCommand("baltop").setTabCompleter(tabCompleter);

        this.getCommand("eco").setExecutor(new EcoCommand(economyProvider, databaseManager));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("DiamondEconomy has been disabled.");
    }
}