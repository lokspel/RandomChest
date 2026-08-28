package me.lokspel.randomchest.command;

import me.lokspel.randomchest.RandomChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher implements CommandExecutor {

    private final RandomChest plugin;
    private final Map<String, SubCommand> subcommands = new LinkedHashMap<>();

    public CommandDispatcher(RandomChest plugin, List<RegisteredCommand> commands) {
        this.plugin = plugin;
        for (RegisteredCommand cmd : commands) {
            subcommands.put(cmd.name(), cmd.executor());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessages().prefixed("usage"));
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(plugin.getMessages().prefixed("usage"));
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        return sub.execute(sender, subArgs);
    }
}
