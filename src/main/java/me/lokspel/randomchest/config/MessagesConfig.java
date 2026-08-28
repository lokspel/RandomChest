package me.lokspel.randomchest.config;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class MessagesConfig {

    private static final String PREFIX_KEY = "prefix";

    private final RandomChest plugin;
    private FileConfiguration config;

    public MessagesConfig(RandomChest plugin) {
        this.plugin = plugin;
        this.config = load();
    }

    private FileConfiguration load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        this.config = load();
    }

    public String get(String key) {
        return color(config.getString(key, ""));
    }

    public String get(String key, String placeholder, String value) {
        return color(config.getString(key, "").replace("%" + placeholder + "%", value));
    }

    public String get(String key, String p1, String v1, String p2, String v2) {
        return color(config.getString(key, "")
                .replace("%" + p1 + "%", v1)
                .replace("%" + p2 + "%", v2));
    }

    public String prefixed(String key) {
        return color(replacePrefix(config.getString(key, "")));
    }

    public String prefixed(String key, String placeholder, String value) {
        return color(replacePrefix(config.getString(key, ""))
                .replace("%" + placeholder + "%", value));
    }

    private String replacePrefix(String text) {
        return text.replace("%prefix%", config.getString(PREFIX_KEY, ""));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
