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

package io.github.pulsebeat02.deluxemediaplugin.utility;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class CommandUtilities {

  private static final HashMap<String, Command> knownCommands;

  static {
    final String ver = Bukkit.getVersion();
    if (Pattern.compile("1\\.(?:13|14|15|16|17)").matcher(ver).find()) {
      final Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
      final SimpleCommandMap commandMap = (SimpleCommandMap) result;
      final Object map = getPrivateField(Objects.requireNonNull(commandMap), "knownCommands");
      knownCommands = (HashMap<String, Command>) map;
    } else if (Pattern.compile("1\\.(?:8|9|10|11|12)").matcher(ver).find()) {
      final Object result =
          getPrivateFieldLegacy(Bukkit.getServer().getPluginManager(), "commandMap");
      final SimpleCommandMap commandMap = (SimpleCommandMap) result;
      final Object map = getPrivateFieldLegacy(Objects.requireNonNull(commandMap), "knownCommands");
      knownCommands = (HashMap<String, Command>) map;
    } else {
      Bukkit.getLogger()
          .log(
              Level.SEVERE,
              "[DeluxeMediaPlugin] You are using a severely "
                  + "outdated version of Minecraft. Please update as versions below 1.8 are not "
                  + "supported. We are sorry of the inconvenience.");
      DeluxeMediaPlugin.OUTDATED = true;
      knownCommands = null;
    }
  }

  public static void ensureInit() {
    Bukkit.getLogger().log(Level.INFO, "[DeluxeMediaPlugin] Ensuring Initialization...");
  }

  public static void unRegisterBukkitCommand(
      @NotNull final DeluxeMediaPlugin plugin, final BaseCommand cmd) {
    if (cmd == null) {
      return;
    }
    try {
      knownCommands.remove(cmd.getName());
      for (final String alias : cmd.getAliases()) {
        if (knownCommands.containsKey(alias)
            && knownCommands.get(alias).toString().contains(plugin.getName())) {
          knownCommands.remove(alias);
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static Object getPrivateField(@NotNull final Object object, @NotNull final String field) {
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
      return result;
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Object getPrivateFieldLegacy(
      @NotNull final Object object, @NotNull final String field) {
    try {
      final Class<?> clazz = object.getClass();
      final Field objectField = clazz.getDeclaredField(field);
      objectField.setAccessible(true);
      final Object result = objectField.get(object);
      objectField.setAccessible(false);
      return result;
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }
}
