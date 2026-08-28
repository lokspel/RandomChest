package me.lokspel.randomchest.util;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public final class ChestDatabase {

    private final RandomChest plugin;
    private YamlConfiguration config;

    public ChestDatabase(RandomChest plugin) {
        this.plugin = plugin;
        this.config = load();
    }

    private YamlConfiguration load() {
        File file = new File(plugin.getDataFolder(), "chestDB.yml");
        if (!file.exists()) {
            YamlConfiguration empty = new YamlConfiguration();
            save(empty);
            return empty;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        this.config = load();
    }

    public String getChestType(Block block) {
        String key = toKey(block);
        return config.contains(key) ? config.getString(key) : null;
    }

    public void setChestType(Block block, String type) {
        config.set(toKey(block), type);
    }

    public void removeChest(Block block) {
        config.set(toKey(block), null);
    }

    public void save() {
        save(config);
    }

    public Set<String> getAllKeys() {
        if (config.getConfigurationSection("chest") == null) {
            return Collections.emptySet();
        }
        return config.getConfigurationSection("chest").getKeys(false);
    }

    public Block getBlock(String key) {
        String[] parts = key.split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return world.getBlockAt(
                parseInt(parts[1]),
                parseInt(parts[2]),
                parseInt(parts[3])
        );
    }

    private String toKey(Block block) {
        return "chest." + block.getWorld().getName()
                + "," + block.getX()
                + "," + block.getY()
                + "," + block.getZ();
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void save(YamlConfiguration cfg) {
        File file = new File(plugin.getDataFolder(), "chestDB.yml");
        try {
            cfg.save(file);
        } catch (IOException ignored) {
        }
    }
}
