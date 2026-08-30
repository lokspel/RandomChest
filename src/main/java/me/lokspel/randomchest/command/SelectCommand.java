package me.lokspel.randomchest.command;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectCommand implements SubCommand {

    private final RandomChest plugin;

    public SelectCommand(RandomChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessages().prefixed("only-game"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessages().prefixed("usage"));
            return true;
        }

        String typeName = args[0];

        if (!plugin.getConfig().contains("chestset." + typeName)) {
            sender.sendMessage(plugin.getMessages().prefixed("type-not-found", "type", typeName));
            return true;
        }

        plugin.getChestUtil().selectType(player, typeName);
        Material tool = plugin.getChestUtil().getToolMaterial("select-tool");
        String toolName = tool != null ? tool.name() : "UNKNOWN";
        sender.sendMessage(plugin.getMessages().get("selected",
                "type", typeName,
                "item", toolName));
        return true;
    }
}
