package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import com.cubixlevels.MessagesManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DistanceListener implements Listener {

    private final CubixLevels plugin;
    private final Map<UUID, Location> lastPosition = new HashMap<>();
    private final Map<UUID, Double> cumulativeDistance = new HashMap<>();

    public DistanceListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Location last = lastPosition.get(uuid);
        if (last == null) {
            lastPosition.put(uuid, event.getFrom().clone());
            return;
        }

        double dx = event.getTo().getX() - last.getX();
        double dz = event.getTo().getZ() - last.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 1.0) return;

        double cumulative = cumulativeDistance.getOrDefault(uuid, 0.0);
        cumulative += distance;
        cumulativeDistance.put(uuid, cumulative);

        int interval = plugin.getConfig().getInt("settings.distance-interval", 1000);
        int xpAmount = plugin.getConfig().getInt("settings.xp-per-distance-interval", 5);

        while (cumulative >= interval) {
            cumulative -= interval;
            cumulativeDistance.put(uuid, cumulative);
            plugin.getPlayerDataManager().addXp(uuid, xpAmount, player);
            player.sendMessage(MessagesManager.format("xp.distance", "§7🚶 §a+{amount} XP §7(§7{blocks} blocks)",
                    "amount", String.valueOf(xpAmount), "blocks", String.valueOf(interval)));
        }

        lastPosition.put(uuid, event.getTo().clone());
    }
}
