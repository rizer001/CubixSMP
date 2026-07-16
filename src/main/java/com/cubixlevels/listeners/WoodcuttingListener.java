package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class WoodcuttingListener implements Listener {

    private final CubixLevels plugin;

    public WoodcuttingListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("woodcutting.enabled", true)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        // Check if it's a log
        double xp = plugin.getConfig().getDouble("woodcutting.logs." + type.name(), 0);
        if (xp <= 0) return;

        // Check natural
        if (!plugin.getNaturalCheck().isNaturalLog(block)) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage("§7🌲 §a+" + formatXp(xp) + " XP §7(Рубка деревьев)");
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
