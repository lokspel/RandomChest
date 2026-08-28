package me.lokspel.randomchest.command;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnselectCommand implements SubCommand {

    private final RandomChest plugin;

    public UnselectCommand(RandomChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessages().prefixed("only-game"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getChestUtil().removeSelectedType(player);
        sender.sendMessage(plugin.getMessages().prefixed("unselect"));
        return true;
    }
}
