package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class MiningListener implements Listener {

    private final CubixLevels plugin;

    public MiningListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("mining.enabled", true)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        // Check config for XP value
        double xp = getXpForBlock(type);
        if (xp <= 0) return;

        // Only natural ores
        if (!plugin.getNaturalCheck().isNaturalOre(block)) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage("§7⛏ §a+" + formatXp(xp) + " XP §7(" + getBlockName(type) + ")");
    }

    private double getXpForBlock(Material mat) {
        String key = mat.name();
        return plugin.getConfig().getDouble("mining.blocks." + key, 0);
    }

    private String getBlockName(Material mat) {
        String name = mat.name().toLowerCase().replace('_', ' ');
        // Remove "deepslate_" prefix for cleaner display
        name = name.replace("deepslate ", "");
        name = name.replace("nether ", "");
        // Capitalize first letter
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
