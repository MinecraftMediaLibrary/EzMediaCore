package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandStructureBuilder {

    private final DeluxeMediaPlugin plugin;

    public CommandStructureBuilder(@NotNull final DeluxeMediaPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO: Create all the command trees

    public void initializeVideoCommand() {
        AbstractCommand dither = new AbstractCommand(plugin, null, "video", "");
        AbstractCommand youtube;
    }

    public void initializeDitherCommand() {
        AbstractCommand dither = new AbstractCommand(plugin, null, "dither", "");
        AbstractCommand settings = new AbstractCommand(plugin, null, "list", "[Dither Setting]") {
            @Override
            public void performCommandTask(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String s, final @NotNull String[] args) {
                sender.sendMessage(ChatColor.GOLD + "List of Possible Options:");
                for (final DitherSetting setting : DitherSetting.values()) {
                    sender.sendMessage(ChatColor.AQUA + setting.name());
                }
                addHistoryEntry(args[0]);
            }
        };
        dither.addChild(settings);
    }

}
