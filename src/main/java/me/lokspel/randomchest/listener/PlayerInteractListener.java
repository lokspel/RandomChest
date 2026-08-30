package me.lokspel.randomchest.listener;

import me.lokspel.randomchest.RandomChest;
import me.lokspel.randomchest.util.ChestUtil;
import me.lokspel.randomchest.util.ReflectionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private final RandomChest plugin;
    private final ChestUtil utils;

    public PlayerInteractListener(RandomChest plugin) {
        this.plugin = plugin;
        this.utils = plugin.getChestUtil();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        Material selectTool = utils.getToolMaterial("select-tool");
        Material removeTool = utils.getToolMaterial("remove-tool");

        if (action.equals(Action.RIGHT_CLICK_BLOCK) && block.getType().equals(Material.CHEST)) {
            handleRightClick(player, event, block, selectTool, removeTool);
            return;
        }

        if (action.equals(Action.LEFT_CLICK_BLOCK) && block.getType().equals(Material.CHEST)) {
            handleLeftClick(player, event, block);
        }
    }

    private void handleRightClick(Player player, PlayerInteractEvent event, Block block,
                                  Material selectTool, Material removeTool) {
        Chest chest = (Chest) block.getState();

        if (utils.haveSelectedType(player)
                && selectTool != null && ReflectionUtil.getItemInHand(player).getType().equals(selectTool)
                && player.getGameMode().equals(GameMode.CREATIVE)) {
            utils.addChest(player, block);
            player.sendMessage(plugin.getMessages().prefixed("add"));
            event.setCancelled(true);
            return;
        }

        if (removeTool != null
                && ReflectionUtil.getItemInHand(player).getType().equals(removeTool)
                && player.getGameMode().equals(GameMode.CREATIVE)) {
            utils.removeChest(block);
            player.sendMessage(plugin.getMessages().prefixed("remove"));
            event.setCancelled(true);
            return;
        }

        String chestType = utils.getChestType(block);
        if (chestType == null) {
            return;
        }

        if (!player.hasPermission("randomchest.use")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().prefixed("no-permission"));
            return;
        }

        if (!utils.isSetExist(chestType)) {
            player.sendMessage(plugin.getMessages().prefixed("type-not-found", "type", chestType));
            return;
        }

        int respawnDelay = utils.getRespawnDelay(chestType);
        if (utils.canRefill(chest)) {
            utils.fill(chest, chestType);
            utils.addRefillDelay(chest, (long) respawnDelay * 1000);
        }
    }

    private void handleLeftClick(Player player, PlayerInteractEvent event, Block block) {
        Chest chest = (Chest) block.getState();
        String chestType = utils.getChestType(block);

        if (chestType == null) {
            return;
        }

        if (!player.hasPermission("randomchest.use")) {
            event.setCancelled(true);
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
