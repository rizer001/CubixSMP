package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FarmingListener implements Listener {

    private final CubixLevels plugin;

    public FarmingListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("farming.enabled", true)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        // Check crops
        double xp = getXpForCrop(type);
        if (xp > 0) {
            // Only fully grown crops give XP
            if (block.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) return;
            }
            grantXp(player, xp, getCropName(type));
            return;
        }

        // Check pumpkins and melons (no ageable data)
        if (type == Material.PUMPKIN) {
            grantXp(player, plugin.getConfig().getDouble("farming.crops.PUMPKIN", 1.0), "Тыква");
        } else if (type == Material.MELON) {
            grantXp(player, plugin.getConfig().getDouble("farming.crops.MELON", 1.0), "Арбуз");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBerryPick(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("farming.enabled", true)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (type == Material.SWEET_BERRY_BUSH) {
            // Check if bush has berries
            if (event.getClickedBlock().getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() >= 2) { // age 2=with berries, 3=full
                    Player player = event.getPlayer();
                    grantXp(player, plugin.getConfig().getDouble("farming.crops.SWEET_BERRY_BUSH", 0.5), "Ягоды");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("farming.enabled", true)) return;
        Material type = event.getBlockState().getType();
        if (type != Material.BEE_NEST && type != Material.BEEHIVE) return;
        for (var item : event.getItems()) {
            Material dropType = item.getItemStack().getType();
            if (dropType == Material.HONEY_BOTTLE)
                grantXp(event.getPlayer(), plugin.getConfig().getDouble("farming.honey.HONEY_BOTTLE", 5.0), "Мёд");
            else if (dropType == Material.HONEYCOMB)
                grantXp(event.getPlayer(), plugin.getConfig().getDouble("farming.honey.HONEYCOMB", 2.0), "Соты");
        }
    }

    private double getXpForCrop(Material mat) {
        String key = mat.name();
        return plugin.getConfig().getDouble("farming.crops." + key, 0);
    }

    private String getCropName(Material mat) {
        return switch (mat) {
            case WHEAT -> "Пшеница";
            case CARROTS -> "Морковь";
            case POTATOES -> "Картошка";
            default -> {
                String name = mat.name().toLowerCase().replace('_', ' ');
                yield Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
        };
    }

    private void grantXp(Player player, double xp, String name) {
        if (xp <= 0) return;
        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        player.sendMessage("§7🌾 §a+" + formatXp(xp) + " XP §7(" + name + ")");
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
