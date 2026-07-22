package com.cubixsmp.listeners;

import com.cubixsmp.CubixSMP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Слушатель, который отключает потерю прочности топора при ломании листвы.
 * Полезно при рубке деревьев — не нужно убирать топор из руки, чтобы
 * пробиться сквозь листву к брёвнам.
 *
 * Настройка: woodcutting.axe-no-durability-loss в config.yml
 */
public class LeafDurabilityListener implements Listener {

    private final CubixSMP plugin;
    private final Set<UUID> breakingLeaves = new HashSet<>();

    public LeafDurabilityListener(CubixSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("woodcutting.axe-no-durability-loss", true)) return;

        Material type = event.getBlock().getType();
        if (!isLeaves(type)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isAxe(item.getType())) return;

        UUID uuid = player.getUniqueId();
        breakingLeaves.add(uuid);

        // Очищаем флаг на следующий тик (PlayerItemDamageEvent уже отстрелял)
        Bukkit.getScheduler().runTask(plugin, () -> breakingLeaves.remove(uuid));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!plugin.getConfig().getBoolean("woodcutting.axe-no-durability-loss", true)) return;

        UUID uuid = event.getPlayer().getUniqueId();
        if (!breakingLeaves.contains(uuid)) return;

        ItemStack item = event.getItem();
        if (!isAxe(item.getType())) return;

        event.setCancelled(true);
    }

    private boolean isLeaves(Material type) {
        return type.name().endsWith("_LEAVES");
    }

    private boolean isAxe(Material type) {
        return type.name().endsWith("_AXE");
    }
}
