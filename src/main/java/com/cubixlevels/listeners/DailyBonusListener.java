package com.cubixlevels.listeners;

import com.cubixlevels.CubixLevels;
import com.cubixlevels.MessagesManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

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
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    List<String> lines = MessagesManager.getStringList("daily_bonus.available",
                            List.of("", "§6☀ §eDaily bonus available! §7(/cubixlevel daily)",
                                    "§7Get §a{amount} XP", ""));
                    int amount = plugin.getConfig().getInt("settings.daily-bonus-xp", 50);
                    for (String line : lines) {
                        player.sendMessage(MessagesManager.replace(line, "amount", String.valueOf(amount)));
                    }
                }
            }, 40L);
        }
    }
}
