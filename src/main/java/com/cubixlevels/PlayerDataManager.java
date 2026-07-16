package com.cubixlevels;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final CubixLevels plugin;
    private final Map<UUID, PlayerData> dataMap = new HashMap<>();

    private static class PlayerData {
        int level;
        double xp;
        int totalPlaytimeSeconds;
        long lastDailyBonusDay;  // day-of-year when daily bonus was last claimed

        PlayerData() {
            this.level = 0;
            this.xp = 0;
            this.totalPlaytimeSeconds = 0;
            this.lastDailyBonusDay = 0;
        }

        PlayerData(int level, double xp, int totalPlaytimeSeconds, long lastDailyBonusDay) {
            this.level = level;
            this.xp = xp;
            this.totalPlaytimeSeconds = totalPlaytimeSeconds;
            this.lastDailyBonusDay = lastDailyBonusDay;
        }
    }

    public PlayerDataManager(CubixLevels plugin) {
        this.plugin = plugin;
        // Listen for join/quit
        plugin.getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                load(e.getPlayer().getUniqueId());
            }
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
                save(e.getPlayer().getUniqueId());
                dataMap.remove(e.getPlayer().getUniqueId());
            }
        }, plugin);
    }

    public void loadAll() {
        File folder = plugin.getPlayerDataFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            String name = f.getName().replace(".yml", "");
            try {
                UUID uuid = UUID.fromString(name);
                load(uuid);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void load(UUID uuid) {
        File file = getFile(uuid);
        if (!file.exists()) {
            dataMap.put(uuid, new PlayerData());
            syncToManagers(uuid);
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(
                config.getInt("level", 0),
                config.getDouble("xp", 0),
                config.getInt("playtime", 0),
                config.getLong("daily-bonus-day", 0)
        );
        dataMap.put(uuid, data);
        syncToManagers(uuid);
    }

    public void save(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        if (data == null) return;
        File file = getFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        config.set("level", data.level);
        config.set("xp", data.xp);
        config.set("playtime", data.totalPlaytimeSeconds);
        config.set("daily-bonus-day", data.lastDailyBonusDay);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : dataMap.keySet()) {
            save(uuid);
        }
    }

    // --- Getters/Setters with auto-sync ---

    public int getLevel(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        return data == null ? 0 : data.level;
    }

    public double getXp(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        return data == null ? 0 : data.xp;
    }

    public void setLevel(UUID uuid, int level) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.level = level;
        plugin.getLevelManager().setLevel(uuid, level);
    }

    public void setXp(UUID uuid, double xp) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.xp = xp;
        plugin.getLevelManager().setXp(uuid, xp);
    }

    public void addXp(UUID uuid, double amount, org.bukkit.entity.Player player) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.xp += amount;
        plugin.getLevelManager().setXp(uuid, data.xp);

        // Check for level-up
        int levelsGained = 0;
        while (data.xp >= plugin.getLevelManager().getXpForNextLevel(data.level)
                && data.level < plugin.getLevelManager().getMaxLevel()) {
            data.xp -= plugin.getLevelManager().getXpForNextLevel(data.level);
            data.level++;
            levelsGained++;
        }

        if (levelsGained > 0) {
            setLevel(uuid, data.level);
            setXp(uuid, data.xp);
            player.sendMessage("§a✦ §eLevel Up! §7Теперь ты §e" + data.level + " §7уровня! §a✦");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    public int getPlaytimeSeconds(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        return data == null ? 0 : data.totalPlaytimeSeconds;
    }

    public void addPlaytime(UUID uuid, int seconds) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.totalPlaytimeSeconds += seconds;

        // Check playtime XP interval
        int interval = plugin.getConfig().getInt("settings.playtime-interval", 1800);
        int xpAmount = plugin.getConfig().getInt("settings.xp-per-playtime-interval", 10);
        if (data.totalPlaytimeSeconds % interval < seconds) {
            // Crossed the interval boundary — grant XP
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                addXp(uuid, xpAmount, player);
                player.sendMessage("§7⏱ §a+" + xpAmount + " XP §7за игру (" + (interval / 60) + " мин)");
            }
        }
    }

    public boolean canClaimDailyBonus(UUID uuid) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        long today = java.time.LocalDate.now().toEpochDay();
        return data.lastDailyBonusDay != today;
    }

    public void claimDailyBonus(UUID uuid, org.bukkit.entity.Player player) {
        PlayerData data = dataMap.computeIfAbsent(uuid, k -> new PlayerData());
        data.lastDailyBonusDay = java.time.LocalDate.now().toEpochDay();
        int xp = plugin.getConfig().getInt("settings.daily-bonus-xp", 50);
        addXp(uuid, xp, player);
        player.sendMessage("§6☀ §eЕжедневный бонус: §a+" + xp + " XP");
    }

    public void syncToManagers(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        if (data == null) return;
        plugin.getLevelManager().setLevel(uuid, data.level);
        plugin.getLevelManager().setXp(uuid, data.xp);
    }

    private File getFile(UUID uuid) {
        return new File(plugin.getPlayerDataFolder(), uuid.toString() + ".yml");
    }
}
