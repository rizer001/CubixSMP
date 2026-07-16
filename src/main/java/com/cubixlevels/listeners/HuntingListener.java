package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class HuntingListener implements Listener {

    private final CubixLevels plugin;

    public HuntingListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("hunting.enabled", true)) return;

        // Check if killed by a player
        Player player = event.getEntity().getKiller();
        if (player == null) return;

        // Check if mob is natural (not from spawner, not from egg)
        if (!plugin.getNaturalCheck().isNaturalMob(event.getEntity())) return;

        // Get XP from config
        EntityType type = event.getEntityType();
        double xp = plugin.getConfig().getDouble("hunting.mobs." + type.name(), 0);
        if (xp <= 0) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage("§7⚔ §a+" + formatXp(xp) + " XP §7(" + getMobName(type) + ")");
    }

    private String getMobName(EntityType type) {
        String name = type.name().toLowerCase().replace('_', ' ');
        // Capitalize
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
