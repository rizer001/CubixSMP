package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import com.cubixlevels.MessagesManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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

        double xp = getXpForBlock(type);
        if (xp <= 0) return;

        if (!plugin.getNaturalCheck().isNaturalOre(block)) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage(MessagesManager.format("xp.mining", "§7⛏ §a+{amount} XP §7(Mining)",
                "amount", formatXp(xp)));
    }

    private double getXpForBlock(Material mat) {
        return plugin.getConfig().getDouble("mining.blocks." + mat.name(), 0);
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
