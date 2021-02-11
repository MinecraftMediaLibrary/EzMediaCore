package com.github.pulsebeat02.deluxemediaplugin.command.commandredo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommandNode {

    private final String name;

    public AbstractCommandNode(@NotNull final String name) {
        this.name = name;
    }

    public abstract boolean performCommandAction(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final @NotNull String[] args);

    public String getName() {
        return name;
    }

}
