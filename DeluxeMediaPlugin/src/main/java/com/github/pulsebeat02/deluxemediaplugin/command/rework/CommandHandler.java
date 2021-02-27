package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandHandler implements TabExecutor {

    private final CommandDispatcher<Object> dispatcher;
    private final RootCommandNode<Object> rootNode;
    private final Set<BaseCommand> commands;

    public CommandHandler(@NotNull final DeluxeMediaPlugin plugin) {
        dispatcher = new CommandDispatcher<>();
        rootNode = dispatcher.getRoot();
        commands = new HashSet<>();
        final CommandMap commandMap = CommandMapHelper.getCommandMap(); // calls CraftServer#getCommandMap() with reflection BTS
        for (final BaseCommand command : commands) {
            rootNode.addChild(command.getCommandNode());
            commandMap.register(plugin.getName(), command);
        }
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        final String joined = command.getName() + ' ' + String.join(" ", args);
        final ParseResults<Object> results = this.dispatcher.parse(joined, sender);
        try {
            dispatcher.execute(results);
        } catch (final CommandSyntaxException exception) {
            sender.sendMessage(((BaseCommand) command).usage());
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        return dispatcher.getCompletionSuggestions(/* something, parsed results probably */)
                .join()
                .asList() // or getList?
                .stream()
                .map(Suggestion::getText) // I think?
                .collect(Collectors.toList());
    }
}