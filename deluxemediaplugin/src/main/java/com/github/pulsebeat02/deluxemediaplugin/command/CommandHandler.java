package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.command.audio.AudioCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.dither.DitherCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.image.ImageCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommand;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandHandler implements TabExecutor {

  private final CommandDispatcher<CommandSender> dispatcher;
  private final RootCommandNode<CommandSender> rootNode;
  private final Set<BaseCommand> commands;
  private final DeluxeMediaPlugin plugin;

  public CommandHandler(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    dispatcher = new CommandDispatcher<>();
    rootNode = dispatcher.getRoot();
    commands =
        ImmutableSet.of(
            new ImageCommand(plugin, this),
            new DitherCommand(plugin, this),
            new VideoCommand(plugin, this),
            new AudioCommand(plugin, this));
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
      final String @NotNull [] args) {
    try {
      dispatcher.execute(
          dispatcher.parse(command.getName() + ' ' + String.join(" ", args).trim(), sender));
    } catch (final CommandSyntaxException exception) {
      plugin.getAudiences().sender(sender).sendMessage(((BaseCommand) command).usage());
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
      final String @NotNull [] args) {
    return dispatcher
        .getCompletionSuggestions(
            dispatcher.parse(command.getName() + ' ' + String.join(" ", args), sender))
        .join()
        .getList()
        .stream()
        .map(Suggestion::getText)
        .collect(Collectors.toList());
  }

  /**
   * Gets dispatcher.
   *
   * @return dispatcher
   */
  public CommandDispatcher<CommandSender> getDispatcher() {
    return dispatcher;
  }

  /**
   * Gets root node.
   *
   * @return root node
   */
  public RootCommandNode<CommandSender> getRootNode() {
    return rootNode;
  }

  /**
   * Gets commands.
   *
   * @return commands
   */
  public Set<BaseCommand> getCommands() {
    return commands;
  }
}