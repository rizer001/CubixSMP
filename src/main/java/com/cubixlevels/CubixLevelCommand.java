package com.cubixlevels;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CubixLevelCommand implements CommandExecutor {

    private final CubixLevels plugin;

    public CubixLevelCommand(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MessagesManager.getString("general.player_only", "§c❌ Only players can use this command!"));
                return true;
            }
            showStats(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("cubixlevels.reload")) {
                    sender.sendMessage(MessagesManager.getString("general.no_permission", "§c❌ You don't have permission!"));
                    return true;
                }
                plugin.reloadConfig();
                MessagesManager.reload();
                plugin.getLevelManager().reload();
                // Resync all online players' cached values
                for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                    plugin.getPlayerDataManager().syncToManagers(p.getUniqueId());
                }
                sender.sendMessage(MessagesManager.getString("general.config_reloaded", "§a✔ Configuration reloaded!"));
            }
            case "daily" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(MessagesManager.getString("general.player_only", "§c❌ Only players can use this command!"));
                    return true;
                }
                if (plugin.getPlayerDataManager().canClaimDailyBonus(player.getUniqueId())) {
                    plugin.getPlayerDataManager().claimDailyBonus(player.getUniqueId(), player);
                } else {
                    player.sendMessage(MessagesManager.getString("general.already_daily", "§c❌ You already claimed daily bonus today!"));
                }
            }
            case "stats" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(MessagesManager.getString("general.player_only", "§c❌ Only players can use this command!"));
                    return true;
                }
                showStats(player);
            }
            default -> {
                if (sender.hasPermission("cubixlevels.admin")) {
                    for (String line : MessagesManager.getStringList("command.help_admin",
                            List.of("§e/cubixlevel §7— stats", "§e/cubixlevel stats §7— stats", "§e/cubixlevel daily §7— daily bonus", "§e/cubixlevel reload §7— reload config"))) {
                        sender.sendMessage(line);
                    }
                } else {
                    String version = plugin.getDescription().getVersion();
                    sender.sendMessage(MessagesManager.format("command.help_header", "§6CubixLevels §7v{version}", "version", version));
                    for (String line : MessagesManager.getStringList("command.help_player",
                            List.of("§e/cubixlevel §7— stats", "§e/cubixlevel daily §7— daily bonus"))) {
                        sender.sendMessage(line);
                    }
                }
            }
        }
        return true;
    }

    private void showStats(Player player) {
        var uuid = player.getUniqueId();
        int level = plugin.getLevelManager().getLevel(uuid);
        double xp = plugin.getLevelManager().getXp(uuid);
        double needed = plugin.getLevelManager().getXpForNextLevel(level);
        int maxLevel = plugin.getLevelManager().getMaxLevel();

        player.sendMessage(MessagesManager.getString("stats.header", "§6╔══════════════════════════════╗"));
        player.sendMessage(MessagesManager.getString("stats.title", "§6║ §lCubixLevels §r§6— Your progress ║"));
        player.sendMessage(MessagesManager.getString("stats.footer", "§6╚══════════════════════════════╝"));

        player.sendMessage(MessagesManager.format("stats.level", "§e✦ Level: §f{level} §7/ {max}",
                "level", String.valueOf(level), "max", String.valueOf(maxLevel)));

        if (level < maxLevel) {
            int progressPercent = needed > 0 ? (int) ((xp / needed) * 100) : 0;
            player.sendMessage(MessagesManager.format("stats.xp", "§e✦ XP: §f{xp} §7/ {needed} XP",
                    "xp", formatXp(xp), "needed", formatXp(needed)));
            player.sendMessage(MessagesManager.format("stats.progress", "§e✦ Progress: §f{percent}%",
                    "percent", String.valueOf(Math.min(progressPercent, 100))));
            player.sendMessage(progressBar(progressPercent));
        } else {
            player.sendMessage(MessagesManager.getString("stats.max_level", "§6✦ §lMAX LEVEL! §6✦"));
        }
    }

    private String progressBar(int percent) {
        int bars = Math.min(percent / 10, 10);
        String filled = MessagesManager.getString("stats.progress_bar_filled", "§a■");
        String empty = MessagesManager.getString("stats.progress_bar_empty", "§7■");
        String bracket = MessagesManager.getString("stats.progress_bar_bracket", "§7[");
        StringBuilder sb = new StringBuilder(bracket);
        for (int i = 0; i < bars; i++) sb.append(filled);
        for (int i = bars; i < 10; i++) sb.append(empty);
        sb.append("§7]");
        return sb.toString();
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
