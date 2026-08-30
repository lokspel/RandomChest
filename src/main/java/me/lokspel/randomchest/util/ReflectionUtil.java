package me.lokspel.randomchest.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Cross-version helpers that tunnel through API differences between legacy
 * (1.8) and modern servers without hard-coding either side.
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static void setDamage(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            try {
                meta.getClass().getMethod("setDamage", int.class).invoke(meta, damage);
                item.setItemMeta(meta);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        try {
            ItemStack.class.getMethod("setDurability", short.class).invoke(item, (short) damage);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static int getMaxDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            try {
                int max = (int) meta.getClass().getMethod("getMaxDamage").invoke(meta);
                if (max > 0) {
                    return max;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        try {
            return ((Number) Material.class.getMethod("getMaxDurability").invoke(item.getType())).intValue();
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    public static ItemStack getItemInHand(Player player) {
        PlayerInventory inventory = player.getInventory();
        try {
            return (ItemStack) PlayerInventory.class.getMethod("getItemInMainHand").invoke(inventory);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            return (ItemStack) PlayerInventory.class.getMethod("getItemInHand").invoke(inventory);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static Block getTargetBlock(Player player, int range) {
        try {
            return (Block) LivingEntity.class.getMethod("getTargetBlockExact", int.class).invoke(player, range);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Method legacy = LivingEntity.class.getMethod("getTargetBlock", Set.class, int.class);
            return (Block) legacy.invoke(player, (Set<Material>) null, range);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}