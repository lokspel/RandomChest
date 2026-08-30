package me.lokspel.randomchest.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Builds potion items across server versions without hard-coding the potion
 * API, which changed between 1.8 and modern Paper.
 * <ul>
 *   <li>1.8: uses {@code org.bukkit.potion.Potion} via reflection.</li>
 *   <li>Modern: uses {@link PotionMeta} via reflection.</li>
 * </ul>
 */
public final class PotionUtil {

    private PotionUtil() {
    }

    /**
     * Tries the legacy {@code org.bukkit.potion.Potion} API (1.8). Returns null
     * when that class is unavailable (modern servers).
     */
    public static ItemStack buildLegacy(String type, boolean splash, int level, int amount) {
        try {
            Class<?> potionClass = Class.forName("org.bukkit.potion.Potion");
            Class<?> potionTypeClass = Class.forName("org.bukkit.potion.PotionType");

            Object potionType = resolvePotionType(type == null ? "WATER" : type, potionTypeClass);
            if (potionType == null) {
                return null;
            }

            Constructor<?> ctor = potionClass.getConstructor(potionTypeClass, int.class, boolean.class);
            Object potion = ctor.newInstance(potionType, Math.max(level, 1), splash);

            Method toItemStack = potionClass.getMethod("toItemStack", int.class);
            return (ItemStack) toItemStack.invoke(potion, amount);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Applies a potion effect type onto potion item meta. Silently does nothing
     * when the chosen effect type is not available on this server version.
     */
    public static ItemStack buildMeta(String type, boolean splash, int amount) {
        if (type == null) {
            return new ItemStack(Material.POTION, amount);
        }

        Material material = splash ? splashPotion() : Material.POTION;
        ItemStack item = new ItemStack(material, amount);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) {
            return item;
        }

        try {
            Object potionType = resolvePotionType(type, Class.forName("org.bukkit.potion.PotionType"));
            if (potionType == null) {
                return item;
            }
            Method setBase = meta.getClass().getMethod("setBasePotionType", potionType.getClass());
            setBase.invoke(meta, potionType);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // base potion type not resolvable on this version; item keeps no effect
        }

        item.setItemMeta(meta);
        return item;
    }

    private static Object resolvePotionType(String type, Class<?> potionTypeClass) {
        try {
            Method valueOf = potionTypeClass.getMethod("valueOf", String.class);
            return valueOf.invoke(null, type);
        } catch (InvocationTargetException ignored) {
            // unknown name
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        try {
            Method byName = potionTypeClass.getMethod("byName", String.class);
            return byName.invoke(null, type);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Material splashPotion() {
        try {
            Field splash = Material.class.getField("SPLASH_POTION");
            return (Material) splash.get(null);
        } catch (ReflectiveOperationException ignored) {
            return Material.POTION;
        }
    }
}