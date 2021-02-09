package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DitherCommand extends AbstractCommand implements CommandExecutor {

    public DitherCommand(@NotNull final DeluxeMediaPlugin plugin) {
        super(plugin, "dither", "");
    }

    @Override
    public void performCommandTask(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Valid Arguments: /dither list"));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage(ChatColor.GOLD + "List of Possible Options:");
                for (final DitherSetting setting : DitherSetting.values()) {
                    sender.sendMessage(ChatColor.AQUA + setting.name());
                }
                addHistoryEntry(args[0]);
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("list");
        }
        return null;
    }
}
