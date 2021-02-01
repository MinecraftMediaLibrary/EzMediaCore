package com.github.pulsebeat02.command;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AbstractCommand implements TabCompleter {

    private final DeluxeMediaPlugin plugin;
    private final List<String> history;

    public AbstractCommand(@NotNull final DeluxeMediaPlugin plugin) {
        this.plugin = plugin;
        this.history = new ArrayList<>();
    }

    public void addHistoryEntry(@NotNull final String autofill) {
        history.add(autofill);
    }

    public DeluxeMediaPlugin getPlugin() {
        return plugin;
    }

    public List<String> getHistory() {
        return history;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        return history;
    }

}
