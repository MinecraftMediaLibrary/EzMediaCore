/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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

package io.github.pulsebeat02.deluxemediaplugin.utility;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public final class CommandUtils {

  private static final HashMap<String, Command> knownCommands;

  static {
    knownCommands =
        (HashMap<String, Command>)
            (getPrivateField(
                    (getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap")
                        .orElseThrow(AssertionError::new)),
                    "knownCommands")
                .orElseThrow(AssertionError::new));
  }

  private CommandUtils() {}

  public static void unRegisterBukkitCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final BaseCommand cmd) {
    try {
      knownCommands.remove(cmd.getName());
      for (final String alias : cmd.getAliases()) {
        if (knownCommands.containsKey(alias)
            && knownCommands.get(alias).toString().contains(plugin.getBootstrap().getName())) {
          knownCommands.remove(alias);
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static @NotNull Optional<Object> getPrivateField(
      @NotNull final Object object, @NotNull final String field) {
    try {
      final Class<?> clazz = object.getClass();
      final Field objectField =
          field.equals("commandMap")
              ? clazz.getDeclaredField(field)
              : field.equals("knownCommands")
                  ? clazz.getSuperclass().getDeclaredField(field)
                  : clazz.getDeclaredField(field);
      objectField.setAccessible(true);
      final Object result = objectField.get(object);
      objectField.setAccessible(false);
      return Optional.of(result);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
