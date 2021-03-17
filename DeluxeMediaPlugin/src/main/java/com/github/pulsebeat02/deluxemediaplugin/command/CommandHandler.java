package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandHandler implements TabExecutor {

  private final CommandDispatcher<CommandSender> dispatcher;
  private final RootCommandNode<CommandSender> rootNode;
  private final Set<BaseCommand> commands;

  public CommandHandler(@NotNull final DeluxeMediaPlugin plugin) {
    dispatcher = new CommandDispatcher<>();
    rootNode = dispatcher.getRoot();
    commands = new HashSet<>();
    final CommandMap commandMap = CommandMapHelper.getCommandMap();
    for (final BaseCommand command : commands) {
      rootNode.addChild(command.getCommandNode());
      commandMap.register(plugin.getName(), command);
    }
  }

  /**
   * CommandHandler to read input and execute other commands.
   *
   * @param sender command sender
   * @param command command sent
   * @param label label of command
   * @param args arguments for command
   * @return whether the command usage should be showed up.
   */
  @Override
  public boolean onCommand(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String label,
      @NotNull final String[] args) {
    final String joined = command.getName() + ' ' + String.join(" ", args);
    final ParseResults<CommandSender> results = dispatcher.parse(joined, sender);
    try {
      dispatcher.execute(results);
    } catch (final CommandSyntaxException exception) {
      sender.sendMessage(((BaseCommand) command).usage());
    }
    return true;
  }

  /**
   * Tab handler to handle tab completer.
   *
   * @param sender command sender
   * @param command current command
   * @param alias aliases of command
   * @param args arguments of the command
   * @return list of options.
   */
  @Override
  public List<String> onTabComplete(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String alias,
      @NotNull final String[] args) {
    final String joined = command.getName() + ' ' + String.join(" ", args);
    final ParseResults<CommandSender> results = dispatcher.parse(joined, sender);
    return dispatcher
        .getCompletionSuggestions(results)
        .join()
        .getList()
        .stream()
        .map(Suggestion::getText)
        .collect(Collectors.toList());
  }

}
