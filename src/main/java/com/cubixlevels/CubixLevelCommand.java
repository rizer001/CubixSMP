package com.cubixlevels;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
                sender.sendMessage("§cТолько игрок может использовать эту команду.");
                return true;
            }
            showStats(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("cubixlevels.reload")) {
                    sender.sendMessage("§cУ вас нет прав.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLevelManager().reload();
                // Resync all online players' cached values
                for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                    plugin.getPlayerDataManager().syncToManagers(p.getUniqueId());
                }
                sender.sendMessage("§aКонфигурация CubixLevels перезагружена!");
            }
            case "daily" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cТолько игрок может использовать эту команду.");
                    return true;
                }
                if (plugin.getPlayerDataManager().canClaimDailyBonus(player.getUniqueId())) {
                    plugin.getPlayerDataManager().claimDailyBonus(player.getUniqueId(), player);
                } else {
                    player.sendMessage("§cВы уже получили ежедневный бонус сегодня!");
                }
            }
            case "stats" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cТолько игрок может использовать эту команду.");
                    return true;
                }
                showStats(player);
            }
            default -> {
                if (sender.hasPermission("cubixlevels.admin")) {
                    sender.sendMessage("§6CubixLevels команды:");
                    sender.sendMessage("§e/cubixlevel §7— показать статистику");
                    sender.sendMessage("§e/cubixlevel daily §7— ежедневный бонус");
                    sender.sendMessage("§e/cubixlevel reload §7— перезагрузить конфиг");
                    sender.sendMessage("§e/cubixlevel stats §7— показать статистику");
                } else {
                    sender.sendMessage("§6CubixLevels §7v" + plugin.getDescription().getVersion());
                    sender.sendMessage("§e/cubixlevel §7— показать статистику");
                    sender.sendMessage("§e/cubixlevel daily §7— ежедневный бонус");
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

        player.sendMessage("§6╔══════════════════════════════╗");
        player.sendMessage("§6║ §lCubixLevels §r§6— Твой прогресс ║");
        player.sendMessage("§6╚══════════════════════════════╝");
        player.sendMessage("§e✦ Уровень: §f" + level + " §7/ " + maxLevel);

        if (level < maxLevel) {
            int progress = needed > 0 ? (int) ((xp / needed) * 100) : 0;
            player.sendMessage("§e✦ Опыт: §f" + formatXp(xp) + " §7/ " + formatXp(needed) + " XP");
            player.sendMessage("§e✦ Прогресс: §f" + Math.min(progress, 100) + "%");
            player.sendMessage(progressBar(progress));
        } else {
            player.sendMessage("§e✦ Опыт: §f" + formatXp(xp) + " XP");
            player.sendMessage("§6✦ §lМАКСИМАЛЬНЫЙ УРОВЕНЬ! §6✦");
        }
    }

    private String progressBar(int percent) {
        int bars = Math.min(percent / 10, 10);
        StringBuilder sb = new StringBuilder("§7[");
        for (int i = 0; i < bars; i++) sb.append("§a■");
        for (int i = bars; i < 10; i++) sb.append("§7■");
        sb.append("§7]");
        return sb.toString();
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) return String.valueOf((long) xp);
        return String.format("%.1f", xp);
    }
}
