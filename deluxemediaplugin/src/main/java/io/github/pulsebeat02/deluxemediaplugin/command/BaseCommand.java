/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.deluxemediaplugin.command;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand extends Command implements LiteralCommandSegment<CommandSender> {

  private final TabExecutor executor;
  private final DeluxeMediaPlugin plugin;
  private final BukkitAudiences audience;

  public BaseCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final String name,
      @NotNull final TabExecutor executor,
      @NotNull final String permission,
      @NotNull final String... aliases) {
    super(name);
    this.setPermission(permission);
    this.setAliases(Arrays.asList(aliases));
    this.plugin = plugin;
    this.executor = executor;
    this.audience = plugin.audience();
  }

  public abstract Component usage();

  @Override
  public boolean execute(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return this.executor.onCommand(sender, this, label, args);
  }

  @Override
  public @NotNull List<String> tabComplete(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return requireNonNull(this.executor.onTabComplete(sender, this, label, args));
  }

  public DeluxeMediaPlugin plugin() {
    return this.plugin;
  }

  public BukkitAudiences audience() {
    return this.audience;
  }
}
