package com.github.pulsebeat02.deluxemediaplugin.command.commandredo;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandStructureAssembly {

    private final DeluxeMediaPlugin plugin;

    public CommandStructureAssembly(@NotNull final DeluxeMediaPlugin plugin) {
        this.plugin = plugin;
    }

    public void assembleCommandStructure() {
        AbstractCommandNode video = new AbstractCommandNode(plugin, "video", null) {
            @Override
            public boolean performCommandAction(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String s, final @NotNull String[] args) {

            }
        };



    }

}
