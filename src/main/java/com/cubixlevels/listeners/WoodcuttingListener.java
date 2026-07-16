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

        double xp = plugin.getConfig().getDouble("woodcutting.logs." + type.name(), 0);
        if (xp <= 0) return;

        if (!plugin.getNaturalCheck().isNaturalLog(block)) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage(MessagesManager.format("xp.woodcutting", "§7🌲 §a+{amount} XP §7(Woodcutting)",
                "amount", formatXp(xp)));
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
