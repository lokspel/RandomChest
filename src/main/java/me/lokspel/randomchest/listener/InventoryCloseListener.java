package me.lokspel.randomchest.listener;

import me.lokspel.randomchest.RandomChest;
import me.lokspel.randomchest.util.ChestUtil;
import me.lokspel.randomchest.util.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryCloseListener implements Listener {

    private final RandomChest plugin;
    private final ChestUtil utils;

    public InventoryCloseListener(RandomChest plugin) {
        this.plugin = plugin;
        this.utils = plugin.getChestUtil();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Block block = ReflectionUtil.getTargetBlock(player, 5);

        if (block == null || !block.getType().equals(Material.CHEST)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        String chestType = utils.getChestType(block);

        if (chestType == null) {
            return;
        }

        if (!player.hasPermission("randomchest.use")) {
            player.sendMessage(plugin.getMessages().prefixed("no-permission"));
            return;
        }

        if (!utils.isSetExist(chestType)) {
            return;
        }

        if (utils.isProtected(chestType)) {
            return;
        }

        int respawnDelay = utils.getRespawnDelay(chestType);
        if (utils.canRefill(chest)) {
            utils.fill(chest, chestType);
            utils.addRefillDelay(chest, (long) respawnDelay * 1000);
        }
        utils.addRespawnDelay(chest, (long) respawnDelay * 1000);
        block.setType(Material.AIR);
    }
}
