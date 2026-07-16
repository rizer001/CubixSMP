package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import com.cubixlevels.MessagesManager;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingListener implements Listener {

    private final CubixLevels plugin;

    public FishingListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean("fishing.enabled", true)) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();

        if (event.getCaught() instanceof Item item) {
            var type = item.getItemStack().getType();

            if (type.name().contains("COD") || type.name().contains("SALMON")
                    || type.name().contains("PUFFERFISH") || type.name().contains("TROPICAL_FISH")) {

                double xp = plugin.getConfig().getDouble("fishing.xp-per-catch", 5.0);
                plugin.getPlayerDataManager().addXp(player.getUniqueId(), xp, player);
                player.sendMessage(MessagesManager.format("xp.fishing", "§7🎣 §a+{amount} XP §7(Fishing)",
                        "amount", formatXp(xp)));
            }
        }
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
