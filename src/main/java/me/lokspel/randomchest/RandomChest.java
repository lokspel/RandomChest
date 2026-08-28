package me.lokspel.randomchest;

import me.lokspel.randomchest.command.CommandDispatcher;
import me.lokspel.randomchest.command.RegisteredCommand;
import me.lokspel.randomchest.command.ReloadCommand;
import me.lokspel.randomchest.command.RestoreCommand;
import me.lokspel.randomchest.command.SelectCommand;
import me.lokspel.randomchest.command.UnselectCommand;
import me.lokspel.randomchest.config.MessagesConfig;
import me.lokspel.randomchest.listener.BlockBreakListener;
import me.lokspel.randomchest.listener.InventoryCloseListener;
import me.lokspel.randomchest.listener.PlayerInteractListener;
import me.lokspel.randomchest.util.ChestUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public final class RandomChest extends JavaPlugin {

    private static RandomChest instance;

    private MessagesConfig messages;
    private ChestUtil chestUtil;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new MessagesConfig(this);
        chestUtil = new ChestUtil(this);

        CommandDispatcher dispatcher = new CommandDispatcher(this, Arrays.asList(
                new RegisteredCommand("reload", new ReloadCommand(this)),
                new RegisteredCommand("select", new SelectCommand(this)),
                new RegisteredCommand("unselect", new UnselectCommand(this)),
                new RegisteredCommand("restore", new RestoreCommand(this))
        ));
        Objects.requireNonNull(getCommand("randomchest")).setExecutor(dispatcher);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new InventoryCloseListener(this), this);

        chestUtil.startChestsRespawn();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (chestUtil != null) {
            chestUtil.forceChestsRespawn();
        }
    }

    public static RandomChest getInstance() {
        return instance;
    }

    public MessagesConfig getMessages() {
        return messages;
    }

    public ChestUtil getChestUtil() {
        return chestUtil;
    }
}
