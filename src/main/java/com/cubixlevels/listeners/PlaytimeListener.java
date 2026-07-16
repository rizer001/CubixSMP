package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks playtime. The actual playtime increment is handled by the
 * scheduled task in CubixLevels.tickPlaytime() every 60 seconds.
 * This listener only captures join/quit to sync data loading.
 */
public class PlaytimeListener implements Listener {

    private final CubixLevels plugin;
    private final Map<UUID, Long> joinTimes = new HashMap<>();

    public PlaytimeListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Long joinTime = joinTimes.remove(uuid);
        if (joinTime != null) {
            // Capture remaining time since last tick (≤60s) to not lose partial intervals.
            // This is added to PlaytimeManager which checks if a playtime interval boundary
            // was crossed since the last scheduled tick.
            int secondsSinceLastTick = (int) ((System.currentTimeMillis() - joinTime) / 1000) % 60;
            if (secondsSinceLastTick > 0) {
                plugin.getPlayerDataManager().addPlaytime(uuid, secondsSinceLastTick);
            }
        }
    }
}
