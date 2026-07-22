package com.cubixsmp;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Управляет сообщениями плагина из config.yml (раздел "messages").
 * Все сообщения плагина вынесены в config.yml для удобной настройки и перевода.
 */
public class MessagesManager {

    private static CubixSMP plugin;

    private MessagesManager() {}

    /**
     * Инициализирует MessagesManager.
     */
    public static void init(CubixSMP plugin) {
        MessagesManager.plugin = plugin;
        plugin.getLogger().info("[Messages] Loaded from config.yml");
    }

    /**
     * Перезагружает сообщения из config.yml.
     * Конфиг уже должен быть перезагружен до вызова этого метода.
     */
    public static void reload() {
        plugin.getLogger().info("[Messages] Reloaded from config.yml");
    }

    /**
     * Возвращает строку сообщения из config.yml (раздел messages) по указанному пути.
     *
     * @param path путь в YAML (например "messages.xp.mining")
     * @param def  значение по умолчанию, если путь не найден
     * @return строка сообщения или def, если путь отсутствует
     */
    public static String getString(String path, String def) {
        FileConfiguration config = plugin.getConfig();
        // Автоматически добавляем префикс "messages." если его нет
        String fullPath = path.startsWith("messages.") ? path : "messages." + path;
        return config.getString(fullPath, def);
    }

    /**
     * Возвращает список строк из config.yml (раздел messages) по указанному пути.
     */
    public static List<String> getStringList(String path, List<String> def) {
        FileConfiguration config = plugin.getConfig();
        String fullPath = path.startsWith("messages.") ? path : "messages." + path;
        List<String> list = config.getStringList(fullPath);
        return list.isEmpty() ? def : list;
    }

    /**
     * Заменяет плейсхолдеры в строке.
     * Поддерживаемые плейсхолдеры:
     * {player}, {level}, {xp}, {needed}, {max}, {amount}, {action},
     * {minutes}, {blocks}, {version}, {percent}, {error}
     */
    public static String replace(String message, String... replacements) {
        if (message == null) return "";
        if (replacements.length % 2 != 0) return message;

        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            result = result.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return result;
    }

    /**
     * Удобный метод: получает строку из config.yml и заменяет плейсхолдеры.
     */
    public static String format(String path, String def, String... replacements) {
        return replace(getString(path, def), replacements);
    }

    public static boolean isLoaded() {
        return plugin != null;
    }
}
