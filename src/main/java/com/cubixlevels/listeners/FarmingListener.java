package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import com.cubixlevels.MessagesManager;
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

        double xp = getXpForCrop(type);
        if (xp > 0) {
            if (block.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) return;
            }
            // Трекер: посаженные игроком культуры не дают XP
            if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(block)) {
                return;
            }
            grantXp(player, xp, getCropName(type));
            return;
        }

        if (type == Material.PUMPKIN) {
            if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(block)) {
                return;
            }
            grantXp(player, plugin.getConfig().getDouble("farming.crops.PUMPKIN", 1.0),
                    MessagesManager.getString("names.pumpkin", "Pumpkin"));
        } else if (type == Material.MELON) {
            if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(block)) {
                return;
            }
            grantXp(player, plugin.getConfig().getDouble("farming.crops.MELON", 1.0),
                    MessagesManager.getString("names.melon", "Melon"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBerryPick(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("farming.enabled", true)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (type == Material.SWEET_BERRY_BUSH) {
            if (event.getClickedBlock().getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() >= 2) {
                    // Трекер: посаженные игроком кусты не дают XP
                    if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(event.getClickedBlock())) {
                        return;
                    }
                    grantXp(event.getPlayer(),
                            plugin.getConfig().getDouble("farming.crops.SWEET_BERRY_BUSH", 0.5),
                            MessagesManager.getString("names.berry", "Berries"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("farming.enabled", true)) return;
        Material type = event.getBlockState().getType();
        if (type != Material.BEE_NEST && type != Material.BEEHIVE) return;
        // Трекер: ульи, поставленные игроком, не дают XP
        if (plugin.getPlacedBlockTracker().wasPlacedByPlayer(event.getBlock())) {
            return;
        }
        for (var item : event.getItems()) {
            Material dropType = item.getItemStack().getType();
            if (dropType == Material.HONEY_BOTTLE)
                grantXp(event.getPlayer(),
                        plugin.getConfig().getDouble("farming.honey.HONEY_BOTTLE", 5.0),
                        MessagesManager.getString("names.honey_bottle", "Honey"));
            else if (dropType == Material.HONEYCOMB)
                grantXp(event.getPlayer(),
                        plugin.getConfig().getDouble("farming.honey.HONEYCOMB", 2.0),
                        MessagesManager.getString("names.honeycomb", "Honeycomb"));
        }
    }

    private double getXpForCrop(Material mat) {
        return plugin.getConfig().getDouble("farming.crops." + mat.name(), 0);
    }

    private String getCropName(Material mat) {
        return switch (mat) {
            case WHEAT -> MessagesManager.getString("names.wheats", "Wheat");
            case CARROTS -> MessagesManager.getString("names.carrots", "Carrots");
            case POTATOES -> MessagesManager.getString("names.potatoes", "Potatoes");
            default -> {
                String name = mat.name().toLowerCase().replace('_', ' ');
                yield Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
        };
    }

    private void grantXp(Player player, double xp, String name) {
        if (xp <= 0) return;
        plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
        plugin.setLastAction(player.getUniqueId(), "Farming");
        player.sendMessage(MessagesManager.replace(
                MessagesManager.getString("xp.farming", "§7🌾 §a+{amount} XP §7({action})"),
                "amount", formatXp(xp), "action", name));
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
