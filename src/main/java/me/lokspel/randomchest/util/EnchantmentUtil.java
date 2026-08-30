package me.lokspel.randomchest.util;

import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves {@link Enchantment} by a plain name like "SHARPNESS" or
 * "sharpness". Modern servers resolve namespaced keys, older ones use
 * legacy names (the pre-1.13 Bukkit constant names), so both are tried
 * through reflection with a translation map in between.
 */
public final class EnchantmentUtil {

    private static final Map<String, String> LEGACY_1_8 = new HashMap<>();

    static {
        LEGACY_1_8.put("PROTECTION", "PROTECTION_ENVIRONMENTAL");
        LEGACY_1_8.put("FIRE_PROTECTION", "PROTECTION_FIRE");
        LEGACY_1_8.put("FEATHER_FALLING", "PROTECTION_FALL");
        LEGACY_1_8.put("BLAST_PROTECTION", "PROTECTION_EXPLOSIONS");
        LEGACY_1_8.put("PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE");
        LEGACY_1_8.put("RESPIRATION", "OXYGEN");
        LEGACY_1_8.put("AQUA_AFFINITY", "WATER_WORKER");
        LEGACY_1_8.put("SHARPNESS", "DAMAGE_ALL");
        LEGACY_1_8.put("SMITE", "DAMAGE_UNDEAD");
        LEGACY_1_8.put("BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS");
        LEGACY_1_8.put("UNBREAKING", "DURABILITY");
        LEGACY_1_8.put("EFFICIENCY", "DIG_SPEED");
        LEGACY_1_8.put("FORTUNE", "LOOT_BONUS_BLOCKS");
        LEGACY_1_8.put("LOOTING", "LOOT_BONUS_MOBS");
        LEGACY_1_8.put("POWER", "ARROW_DAMAGE");
        LEGACY_1_8.put("PUNCH", "ARROW_KNOCKBACK");
        LEGACY_1_8.put("FLAME", "ARROW_FIRE");
        LEGACY_1_8.put("INFINITY", "ARROW_INFINITE");
        LEGACY_1_8.put("LUCK_OF_THE_SEA", "LUCK");
    }

    private EnchantmentUtil() {
    }

    public static Enchantment getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String key = name.toUpperCase().replace(' ', '_').replace('-', '_');

        try {
            Class<?> namespacedKey = Class.forName(
                    "org.bukkit.NamespacedKey",
                    false,
                    Enchantment.class.getClassLoader()
            );
            Object nk = namespacedKey.getMethod("minecraft", String.class).invoke(null, key.toLowerCase());
            Method getByKey = Enchantment.class.getMethod("getByKey", namespacedKey);
            Enchantment ench = (Enchantment) getByKey.invoke(null, nk);
            if (ench != null) {
                return ench;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalArgumentException ignored) {
        }

        Enchantment ench = getLegacy(key);
        if (ench != null) {
            return ench;
        }
        String translated = LEGACY_1_8.get(key);
        return translated != null ? getLegacy(translated) : null;
    }

    private static Enchantment getLegacy(String key) {
        try {
            Method getByName = Enchantment.class.getMethod("getByName", String.class);
            return (Enchantment) getByName.invoke(null, key);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | IllegalArgumentException ignored) {
            return null;
        }
    }
}