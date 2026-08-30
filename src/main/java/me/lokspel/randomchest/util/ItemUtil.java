package me.lokspel.randomchest.util;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Item-level helpers that work on ItemStack/ItemStack and raw config maps,
 * keeping item logic out of domain classes.
 */
public final class ItemUtil {

    public static boolean isSword(ItemStack item) {
        return item != null && MaterialUtil.isSword(item.getType());
    }

    public static boolean isBow(ItemStack item) {
        return item != null && MaterialUtil.isBow(item.getType());
    }

    public static boolean isHelmet(ItemStack item) {
        return item != null && MaterialUtil.isHelmet(item.getType());
    }

    public static boolean isBoots(ItemStack item) {
        return item != null && MaterialUtil.isBoots(item.getType());
    }

    public static boolean isChestplate(ItemStack item) {
        return item != null && MaterialUtil.isChestplate(item.getType());
    }

    public static int getInt(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    public static boolean getBoolean(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Boolean && (Boolean) val;
    }
}
