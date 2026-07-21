package com.cubixlevels;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CubixLevelTabCompleter implements TabCompleter {

    private static final List<String> PLAYER_COMMANDS = List.of("daily", "stats", "sound", "leaders");
    private static final List<String> ADMIN_COMMANDS = List.of(
            "reload", "daily", "stats", "sound", "leaders", "admin"
    );
    private static final List<String> ADMIN_SUBCOMMANDS = List.of(
            "info", "setlevel", "addxp", "removexp", "reset"
    );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                 @NotNull Command command,
                                                 @NotNull String alias,
                                                 @NotNull String[] args) {
        if (args.length == 1) {
            // Первый уровень: основные команды
            List<String> commands = new ArrayList<>();
            commands.add("daily");
            commands.add("stats");
            commands.add("sound");
            commands.add("leaders");
            if (sender.hasPermission("cubixlevels.reload")) {
                commands.add("reload");
            }
            if (sender.hasPermission("cubixlevels.admin")) {
                commands.add("admin");
            }
            return filter(commands, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return filter(ADMIN_SUBCOMMANDS, args[1]);
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("admin")) {
            String sub = args[1].toLowerCase();
            switch (sub) {
                case "info":
                case "setlevel":
                case "addxp":
                case "removexp":
                case "reset": {
                    if (args.length == 3) {
                        // Предлагаем имена онлайн-игроков
                        List<String> players = new ArrayList<>();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            players.add(p.getName());
                        }
                        return filter(players, args[2]);
                    }
                    if (args.length == 4 && sub.equals("setlevel")) {
                        // Предлагаем уровни 1-100
                        List<String> levels = new ArrayList<>();
                        int max = CubixLevels.getInstance().getLevelManager().getMaxLevel();
                        for (int i = 1; i <= max; i += 5) {
                            levels.add(String.valueOf(i));
                        }
                        levels.add(String.valueOf(max));
                        return filter(levels, args[3]);
                    }
                    if (args.length == 4 && (sub.equals("addxp") || sub.equals("removexp"))) {
                        // Предлагаем популярные значения XP
                        return filter(List.of("10", "50", "100", "500", "1000"), args[3]);
                    }
                    if (args.length == 4 && sub.equals("reset")) {
                        return filter(List.of("confirm"), args[3]);
                    }
                    break;
                }
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) return options;
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(opt);
            }
        }
        return result;
    }
}
