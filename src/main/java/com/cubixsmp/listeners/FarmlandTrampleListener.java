package com.cubixsmp.listeners;

import com.cubixsmp.CubixSMP;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Слушатель, который отключает вытаптывание грядок (FARMLAND → DIRT)
 * игроками и мобами.
 *
 * Настройка: farming.no-trampling в config.yml
 */
public class FarmlandTrampleListener implements Listener {

    private final CubixSMP plugin;

    public FarmlandTrampleListener(CubixSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (!checkEnabled()) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() != Material.FARMLAND) return;
        if (!checkEnabled()) return;

        // Отменяем только превращение в землю (само вытаптывание)
        if (event.getTo() == Material.DIRT) {
            event.setCancelled(true);
        }
    }

    private boolean checkEnabled() {
        return plugin.getConfig().getBoolean("farming.no-trampling", true);
    }
}
