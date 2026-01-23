package com.mikey1201;

import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;

public final class DiamondEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyProvider economyProvider;

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
        VaultHook.hook(this, databaseManager, getLogger());8D81-E3FA
    }

    @Override
    public void onEnable() {

        if (databaseManager == null || economyProvider == null) {
            getLogger().severe("Critical components failed to load in onLoad. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerAccountListener(databaseManager), this);

        getLogger().info("DiamondEconomy has been enabled!");
    }

    private void registerCommands() {
        TabCompleter tabCompleter = new CommandTabCompleter();

        this.getCommand("balance").setExecutor(new BalanceCommand(economyProvider));
        this.getCommand("balance").setTabCompleter(tabCompleter);

        this.getCommand("deposit").setExecutor(new DepositCommand(economyProvider));
        this.getCommand("deposit").setTabCompleter(tabCompleter);

        this.getCommand("withdraw").setExecutor(new WithdrawCommand(economyProvider));
        this.getCommand("withdraw").setTabCompleter(tabCompleter);

        this.getCommand("pay").setExecutor(new PayCommand(economyProvider));
        this.getCommand("pay").setTabCompleter(tabCompleter);

        this.getCommand("baltop").setExecutor(new BaltopCommand(databaseManager, economyProvider));
        this.getCommand("baltop").setTabCompleter(tabCompleter);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("DiamondEconomy has been disabled.");
    }
}