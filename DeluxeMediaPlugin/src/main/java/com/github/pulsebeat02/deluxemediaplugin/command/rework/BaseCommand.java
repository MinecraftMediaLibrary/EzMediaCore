package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand extends Command
    implements LiteralCommandSegment<CommandSender> {

  protected final TabExecutor executor;

  public BaseCommand(
      @NotNull final String name,
      @NotNull final TabExecutor executor,
      @NotNull final String permission,
      @NotNull final String... aliases) {
    super(name);
    this.setPermission(permission);
    this.setAliases(Arrays.asList(aliases));
    this.executor = executor;
  }

  // subclasses must implement getCommandNode

  public abstract String usage();

  @Override
  public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return executor.onCommand(sender, this, label, args);
  }

  @Override
  public List<String> tabComplete(
          @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    final List<String> list = executor.onTabComplete(sender, this, label, args);
    return list == null ? new ArrayList<>() : list;
  }
}
