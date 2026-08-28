package me.lokspel.randomchest.listener;

import me.lokspel.randomchest.RandomChest;
import me.lokspel.randomchest.util.ChestUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final RandomChest plugin;
    private final ChestUtil utils;

    public BlockBreakListener(RandomChest plugin) {
        this.plugin = plugin;
        this.utils = plugin.getChestUtil();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!block.getType().equals(Material.CHEST)) {
            return;
        }

        String chestType = utils.getChestType(block);
        if (chestType == null) {
            return;
        }

        if (utils.isSetExist(chestType)) {
            event.setCancelled(utils.isProtected(chestType));
        } else {
            player.sendMessage(plugin.getMessages().prefixed("type-not-found", "type", chestType));
        }
    }
}
