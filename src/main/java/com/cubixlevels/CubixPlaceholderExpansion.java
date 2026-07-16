package com.cubixlevels;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CubixPlaceholderExpansion extends PlaceholderExpansion {

    private final CubixLevels plugin;

    public CubixPlaceholderExpansion(CubixLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cubix";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Minecraft337";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Keep registered even after /reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";

        return switch (params.toLowerCase()) {
            case "level" -> String.valueOf(plugin.getLevelManager().getLevel(player.getUniqueId()));
            case "level_xp" -> formatXp(plugin.getLevelManager().getXp(player.getUniqueId()));
            case "level_xp_needed" -> formatXp(plugin.getLevelManager().getXpForNextLevel(
                    plugin.getLevelManager().getLevel(player.getUniqueId())));
            case "level_progress" -> {
                double xp = plugin.getLevelManager().getXp(player.getUniqueId());
                double needed = plugin.getLevelManager().getXpForNextLevel(
                        plugin.getLevelManager().getLevel(player.getUniqueId()));
                if (needed <= 0) yield "100%";
                int percent = (int) ((xp / needed) * 100);
                yield Math.min(percent, 100) + "%";
            }
            case "level_playtime" -> {
                int seconds = plugin.getPlayerDataManager().getPlaytimeSeconds(player.getUniqueId());
                yield formatPlaytime(seconds);
            }
            default -> null;
        };
    }

    private String formatXp(double xp) {
        if (xp == (long) xp) {
            return String.valueOf((long) xp);
        }
        return String.format("%.1f", xp);
    }

    private String formatPlaytime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) {
            return hours + "ч " + minutes + "мин";
        }
        return minutes + "мин";
    }
}
