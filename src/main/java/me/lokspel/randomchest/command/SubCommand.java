package me.lokspel.randomchest.command;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    boolean execute(CommandSender sender, String[] args);
}
