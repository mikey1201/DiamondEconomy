package com.mikey1201;

import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiamondEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyProvider economyProvider;
    private MessageManager messageManager;
    private HiddenPlayersManager hiddenPlayersManager;

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

        getServer().getPluginManager().registerEvents(new PlayerAccountListener(databaseManager), this);
        getLogger().info("DiamondEconomy has been enabled!");
    }

    private Material getCurrencyMaterial() {
        String configName = getConfig().getString("currency-item", "DIAMOND");
        Material material = Material.getMaterial(configName);

        if (material == null || !material.isItem()) {
            getLogger().warning("Invalid material '" + configName + "' in config.yml. Defaulting to DIAMOND.");
            return Material.DIAMOND;
        }
        
        getLogger().info("Currency item set to: " + material.name());
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