package com.cubixsmp.listeners;

import com.cubixsmp.CubixSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Заменяет плейсхолдеры %cubixsmp_...% в чате на значения,
 * соответствующие КАЖДОМУ получателю сообщения.
 *
 * Например, игрок пишет "Мой уровень %cubixsmp_level%"
 * — каждый, кто видит это сообщение, увидит СВОЙ уровень.
 *
 * Настройка: chat-placeholders в config.yml
 */
public class ChatPlaceholderListener implements Listener {

    private final CubixSMP plugin;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%cubixsmp_(\\w+)%");
    private static final String CFG = "chat-placeholders.";

    public ChatPlaceholderListener(CubixSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean(CFG + "enabled", true)) return;

        String raw = event.getMessage();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(raw);
        if (!matcher.find()) return;

        // Отменяем стандартную рассылку — будем отправлять сами
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String format = event.getFormat();
        Player[] recipients = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        // Переключаемся на главный поток — все Bukkit API вызовы там
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Отправляем каждому получателю с его значениями
            for (Player recipient : recipients) {
                String resolved = replacePlaceholders(raw, recipient);
                String msg = format.replace("%1$s", sender.getDisplayName())
                                   .replace("%2$s", resolved);
                recipient.sendMessage(msg);
            }

            // Лог в консоль (с значениями отправителя)
            String consoleMsg = replacePlaceholders(raw, sender);
            String logMsg = format.replace("%1$s", sender.getDisplayName())
                                  .replace("%2$s", consoleMsg);
            Bukkit.getConsoleSender().sendMessage(logMsg);
        });
    }

    /**
     * Заменяет все %cubixsmp_{param}% в строке на значения для указанного игрока.
     */
    private String replacePlaceholders(String message, Player player) {
        if (!plugin.hasPlaceholderAPI() || plugin.getPlaceholderExpansion() == null) {
            return message;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String param = matcher.group(1);
            String value = plugin.getPlaceholderExpansion()
                    .onPlaceholderRequest(player, param);
            if (value == null) {
                value = matcher.group(0); // оставляем как есть
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
