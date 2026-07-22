package com.cubixsmp.listeners;

import com.cubixsmp.CubixSMP;
import com.cubixsmp.MessagesManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final CubixSMP plugin;

    public MiningListener(CubixSMP plugin) {
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

        // Сначала проверяем трекер: если блок поставлен игроком — XP не начисляется
        if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(block)) {
            return;
        }

        // Fallback: статический анализ для блоков, поставленных до установки плагина
        if (!plugin.getNaturalCheck().isNaturalOre(block)) return;

        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        plugin.setLastAction(player.getUniqueId(), "Mining");
        String msg = MessagesManager.format("xp.mining", "§7⛏ §a+{amount} XP §7(Шахтёрство)",
                "amount", formatXp(xp));
        if (plugin.getConfig().getBoolean("settings.use-actionbar", true)) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(msg));
        } else {
            player.sendMessage(msg);
        }
    }

    private double getXpForBlock(Material mat) {
        return plugin.getConfig().getDouble("mining.blocks." + mat.name(), 0);
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
