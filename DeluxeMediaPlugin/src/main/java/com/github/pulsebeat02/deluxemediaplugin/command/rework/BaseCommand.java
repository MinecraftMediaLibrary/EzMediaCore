package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class BaseCommand extends Command implements LiteralCommandSegment<CommandSender> {

  protected final TabExecutor executor;
  private final MinecraftMediaLibrary library;

  /**
   * Instantiates a new BaseCommand.
   *
   * @param name command name
   * @param executor tab complete executor
   * @param permission permission for command
   * @param aliases aliases for command
   */
  public BaseCommand(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String name,
      @NotNull final TabExecutor executor,
      @NotNull final String permission,
      @NotNull final String... aliases) {
    super(name);
    setPermission(permission);
    setAliases(Arrays.asList(aliases));
    this.library = library;
    this.executor = executor;
  }

  /**
   * Returns the correct usage of the command.
   *
   * @return usage
   */
  public abstract String usage();

  /**
   * Executes a specific command from a player.
   *
   * @param sender who requested to send the command
   * @param label label of command
   * @param args arguments of command
   * @return whether command should show usage or not
   */
  @Override
  public boolean execute(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return executor.onCommand(sender, this, label, args);
  }

  /**
   * Gets the tab complete for a player. (Uses Object#requireNonNull to get rid of the dumb error)
   *
   * @param sender who requested the tab complete
   * @param label label of command
   * @param args arguments of tab complete
   * @return tab completed options
   */
  @Override
  public List<String> tabComplete(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return Objects.requireNonNull(executor.onTabComplete(sender, this, label, args));
  }

  public MinecraftMediaLibrary getLibrary() {
    return library;
  }
}
