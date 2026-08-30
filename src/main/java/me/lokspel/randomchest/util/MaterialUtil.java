package me.lokspel.randomchest.util;

import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Universal material resolution and category checks.
 * <p>
 * Resolves a material from a text name (e.g. "DIAMOND_SWORD", "diamond sword")
 * or a legacy numeric ID (e.g. "310"). Because {@link Material#getId()} is not
 * available on newer servers, numeric resolution goes through reflection so the
 * same code keeps working on old (1.8) and new side by side.
 */
public final class MaterialUtil {

    private MaterialUtil() {
    }

    public static Material getMaterial(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return getMaterialById(((Number) value).intValue());
        }

        String name = String.valueOf(value).trim();
        if (name.isEmpty()) {
            return null;
        }

        try {
            return Material.matchMaterial(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static Material getMaterialById(int id) {
        try {
            Method getId = Material.class.getMethod("getId");
            for (Material mat : Material.values()) {
                int matId = (int) getId.invoke(mat);
                if (matId == id) {
                    return mat;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // No numeric IDs available (modern Material) -> treat as unknown.
        }
        return null;
    }

    public static boolean isSword(Material material) {
        return material != null && material.name().endsWith("_SWORD");
    }

    public static boolean isBow(Material material) {
        return material != null && material.name().endsWith("_BOW");
    }

    public static boolean isHelmet(Material material) {
        return material != null && material.name().endsWith("_HELMET");
    }

    public static boolean isChestplate(Material material) {
        return material != null && material.name().endsWith("_CHESTPLATE");
    }

    public static boolean isBoots(Material material) {
        return material != null && material.name().endsWith("_BOOTS");
    }

    public static boolean isPotion(Material material) {
        return material != null && (material == Material.POTION || isSplashPotion(material));
    }

    public static boolean isSplashPotion(Material material) {
        if (material == null) {
            return false;
        }
        try {
            Field splash = Material.class.getField("SPLASH_POTION");
            return material == splash.get(null);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
