package com.cubixsmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Управляет настройками звука пинга для каждого игрока.
 *
 * Данные: plugins/CubixSMP/pingsettings.yml
 */
public class PingSettingsManager {

    private final CubixSMP plugin;
    private final File file;
    private final Set<UUID> pingDisabled = new HashSet<>();

    public PingSettingsManager(CubixSMP plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pingsettings.yml");
        load();
    }

    /** Загружает настройки с диска */
    private void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (!config.getBoolean(key, true)) {
                    pingDisabled.add(uuid);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /** Сохраняет настройки на диск */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (UUID uuid : pingDisabled) {
            config.set(uuid.toString(), false);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[PingSettings] Failed to save", e);
        }
    }

    /** Переключает звук пинга для игрока. Возвращает новое состояние (true = звук вкл) */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (pingDisabled.contains(uuid)) {
            pingDisabled.remove(uuid);
            save();
            return true;
        } else {
            pingDisabled.add(uuid);
            save();
            return false;
        }
    }

    /** true = звук пинга включен для этого игрока */
    public boolean isEnabled(Player player) {
        return !pingDisabled.contains(player.getUniqueId());
    }
}
