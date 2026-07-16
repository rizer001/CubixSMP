package com.cubixlevels;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * Управляет файлом messages.yml — все сообщения плагина вынесены сюда
 * для удобной настройки и перевода.
 */
public class MessagesManager {

    private static final String MESSAGES_FILE = "messages.yml";
    private static FileConfiguration messages;
    private static CubixLevels plugin;

    private MessagesManager() {}

    /**
     * Инициализирует MessagesManager: сохраняет messages.yml из ресурсов
     * (если не существует) и загружает.
     */
    public static void init(CubixLevels plugin) {
        MessagesManager.plugin = plugin;
        saveMessagesFile();
        reload();
        plugin.getLogger().info("[Messages] Loaded: " + MESSAGES_FILE);
    }

    /**
     * Сохраняет messages.yml из ресурсов плагина, если файл ещё не существует.
     */
    private static void saveMessagesFile() {
        File messagesFile = new File(plugin.getDataFolder(), MESSAGES_FILE);
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource(MESSAGES_FILE, false);
                plugin.getLogger().info("[Messages] Created new file: " + MESSAGES_FILE);
            } catch (Exception e) {
                plugin.getLogger().warning("[Messages] Failed to save " + MESSAGES_FILE + " from resources");
            }
        }
    }

    /**
     * Перезагружает messages.yml с диска.
     */
    public static void reload() {
        File messagesFile = new File(plugin.getDataFolder(), MESSAGES_FILE);
        if (messagesFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(messagesFile);
        } else {
            messages = new YamlConfiguration();
        }
    }

    /**
     * Возвращает строку сообщения из messages.yml по указанному пути.
     *
     * @param path путь в YAML (например "xp.mining")
     * @param def  значение по умолчанию, если путь не найден
     * @return строка сообщения или def, если путь отсутствует
     */
    public static String getString(String path, String def) {
        if (messages == null) return def;
        return messages.getString(path, def);
    }

    /**
     * Возвращает список строк из messages.yml по указанному пути.
     */
    public static List<String> getStringList(String path, List<String> def) {
        if (messages == null) return def;
        List<String> list = messages.getStringList(path);
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
     * Удобный метод: получает строку из messages.yml и заменяет плейсхолдеры.
     */
    public static String format(String path, String def, String... replacements) {
        return replace(getString(path, def), replacements);
    }

    public static boolean isLoaded() {
        return messages != null;
    }
}
