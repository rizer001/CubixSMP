package com.cubixlevels;

import com.cubixlevels.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CubixLevels extends JavaPlugin {

    private static CubixLevels instance;
    private LevelManager levelManager;
    private PlayerDataManager playerDataManager;
    private NaturalCheck naturalCheck;
    private CubixPlaceholderExpansion placeholderExpansion;
    private boolean hasPlaceholderAPI;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Load managers
        this.naturalCheck = new NaturalCheck();
        this.playerDataManager = new PlayerDataManager(this);
        this.levelManager = new LevelManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(this), this);
        getServer().getPluginManager().registerEvents(new WoodcuttingListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new HuntingListener(this), this);
        getServer().getPluginManager().registerEvents(new DistanceListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(this), this);
        getServer().getPluginManager().registerEvents(new DailyBonusListener(this), this);

        // Register command
        getCommand("cubixlevel").setExecutor(new CubixLevelCommand(this));

        // PlaceholderAPI hook
        this.hasPlaceholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (hasPlaceholderAPI) {
            this.placeholderExpansion = new CubixPlaceholderExpansion(this);
            if (placeholderExpansion.register()) {
                getLogger().info("PlaceholderAPI expansion registered successfully");
            } else {
                getLogger().warning("Failed to register PlaceholderAPI expansion");
                hasPlaceholderAPI = false;
            }
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders disabled");
        }

        // Load all player data
        playerDataManager.loadAll();

        // Start playtime tracker
        getServer().getScheduler().runTaskTimer(this, this::tickPlaytime, 1200L, 1200L); // every 60s

        getLogger().info("CubixLevels v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        getLogger().info("CubixLevels disabled!");
    }

    private void tickPlaytime() {
        if (!getConfig().getBoolean("settings.playtime-interval", true)) return;
        int interval = getConfig().getInt("settings.playtime-interval", 1800); // seconds
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            playerDataManager.addPlaytime(player.getUniqueId(), 60); // 60 seconds per tick
        }
    }

    // --- Static access ---
    public static CubixLevels getInstance() { return instance; }
    public LevelManager getLevelManager() { return levelManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public NaturalCheck getNaturalCheck() { return naturalCheck; }
    public boolean hasPlaceholderAPI() { return hasPlaceholderAPI; }

    public File getPlayerDataFolder() {
        File folder = new File(getDataFolder(), "playerdata");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }
}
