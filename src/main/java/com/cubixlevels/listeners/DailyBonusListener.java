package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Reminds players about the daily bonus when they join.
 * The actual claiming is done via /cubixlevel daily.
 */
public class DailyBonusListener implements Listener {

    private final CubixLevels plugin;

    public DailyBonusListener(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getPlayerDataManager().canClaimDailyBonus(player.getUniqueId())) {
            // Remind after a short delay to not spam on login
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("");
                    player.sendMessage("§6☀ §eЕжедневный бонус доступен! §7(/cubixlevel daily)");
                    player.sendMessage("§7Получи §a" + plugin.getConfig().getInt("settings.daily-bonus-xp", 50) + " XP");
                    player.sendMessage("");
                }
            }, 40L); // 2 seconds delay
        }
    }
}
