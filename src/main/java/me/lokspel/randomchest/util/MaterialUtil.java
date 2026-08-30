package me.lokspel.randomchest.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Material resolution by legacy ID and category checks.
 */
@SuppressWarnings("deprecation")
public final class MaterialUtil {

    private static final Map<Integer, Material> BY_ID = new HashMap<>();

    static {
        for (Material mat : Material.values()) {
            if (mat.getId() > 0) {
                BY_ID.put(mat.getId(), mat);
            }
        }
    }

    public static Material getMaterialById(int id) {
        return BY_ID.get(id);
    }

    public static boolean isSword(Material mat) {
        return mat != null && mat.name().endsWith("_SWORD");
    }

    public static boolean isBow(Material mat) {
        return mat != null && mat.name().equals("BOW");
    }

    public static boolean isHelmet(Material mat) {
        return mat != null && mat.name().endsWith("_HELMET");
    }

    public static boolean isChestplate(Material mat) {
        String name = mat.name();
        return name.endsWith("_CHESTPLATE") || name.equals("CHAINMAIL_CHESTPLATE");
    }

    public static boolean isBoots(Material mat) {
        return mat != null && mat.name().endsWith("_BOOTS");
    }
}
