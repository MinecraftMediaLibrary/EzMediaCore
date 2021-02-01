package com.github.pulsebeat02.command;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.utility.ChatUtilities;
import com.github.pulsebeat02.video.dither.DitherSetting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DitherCommand extends AbstractCommand implements CommandExecutor {

    public DitherCommand(@NotNull final DeluxeMediaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "You must be a player to use this command!"));
            return true;
        }
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
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (args.length == 0) {
            return Collections.singletonList("list");
        }
        return null;
    }
}
