package com.github.pulsebeat02.command;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AbstractCommand {

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

}
