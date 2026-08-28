package me.lokspel.randomchest.command;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.command.CommandSender;

public class RestoreCommand implements SubCommand {

    private final RandomChest plugin;

    public RestoreCommand(RandomChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(plugin.getMessages().prefixed("only-game"));
            return true;
        }

        plugin.getChestUtil().restoreAllChests();
        sender.sendMessage(plugin.getMessages().prefixed("restore"));
        return true;
    }
}
