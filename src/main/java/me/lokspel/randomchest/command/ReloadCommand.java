package me.lokspel.randomchest.command;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    private final RandomChest plugin;

    public ReloadCommand(RandomChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.getChestUtil().reloadChestDB();
        plugin.getMessages().reload();
        sender.sendMessage(plugin.getMessages().prefixed("reload"));
        return true;
    }
}
