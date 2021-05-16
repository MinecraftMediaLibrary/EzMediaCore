/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class BaseCommand extends Command implements LiteralCommandSegment<CommandSender> {

  protected final TabExecutor executor;
  private final DeluxeMediaPlugin plugin;

  /**
   * Instantiates a new BaseCommand.
   *
   * @param plugin instance
   * @param name command name
   * @param executor tab complete executor
   * @param permission permission for command
   * @param aliases aliases for command
   */
  public BaseCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final String name,
      @NotNull final TabExecutor executor,
      @NotNull final String permission,
      @NotNull final String... aliases) {
    super(name);
    setPermission(permission);
    setAliases(Arrays.asList(aliases));
    this.plugin = plugin;
    this.executor = executor;
  }

  /**
   * Returns the correct usage of the command.
   *
   * @return usage
   */
  public abstract Component usage();

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
  public @NotNull List<String> tabComplete(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return Objects.requireNonNull(executor.onTabComplete(sender, this, label, args));
  }

  public DeluxeMediaPlugin getPlugin() {
    return plugin;
  }
}
