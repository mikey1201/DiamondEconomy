package com.mikey1201;

import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;

public final class DiamondEconomy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyProvider economyProvider;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        databaseManager = new DatabaseManager(getDataFolder(), getLogger());
        try {
            databaseManager.connect();
            databaseManager.initialize();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize the database! Disabling plugin.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economyProvider = new EconomyProvider(databaseManager);

        VaultHook.hook(this, databaseManager, getLogger());

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