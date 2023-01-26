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
package io.github.pulsebeat02.deluxemediaplugin.utility.component;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public final class CommandUtils {

  private static final HashMap<String, Command> knownCommands;

  static {
    final Object map = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
    final Object commands = getPrivateField((map), "knownCommands");
    knownCommands = (HashMap<String, Command>) commands;
  }

  private CommandUtils() {}

  public static void unregisterCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final BaseCommand cmd) {
    knownCommands.remove(cmd.getName());
    cmd.getAliases().forEach(alias -> removeAliases(plugin, alias));
  }

  private static void removeAliases(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String alias) {
    if (containsCommand(alias) && containsPluginName(plugin, alias)) {
      knownCommands.remove(alias);
    }
  }

  private static boolean containsPluginName(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String alias) {
    return knownCommands.get(alias).toString().contains(plugin.getBootstrap().getName());
  }

  private static boolean containsCommand(@NotNull final String alias) {
    return knownCommands.containsKey(alias);
  }

  private static @NotNull Object getPrivateField(
      @NotNull final Object object, @NotNull final String field) {
    try {
      final Field objectField = getField(object.getClass(), field);
      objectField.setAccessible(true);
      return objectField.get(object);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static @NotNull Field getField(@NotNull final Class<?> clazz, @NotNull final String field)
      throws NoSuchFieldException {
    return field.equals("commandMap")
        ? clazz.getDeclaredField(field)
        : field.equals("knownCommands")
            ? clazz.getSuperclass().getDeclaredField(field)
            : clazz.getDeclaredField(field);
  }
}
