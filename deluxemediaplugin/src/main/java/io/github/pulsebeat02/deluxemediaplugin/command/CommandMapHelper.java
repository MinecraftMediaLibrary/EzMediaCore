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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

public final class CommandMapHelper {

  private static final SimpleCommandMap SIMPLE_COMMAND_MAP;

  static {
    try {
      final Class<?> clazz = Bukkit.getServer().getClass();
      final String name = "getCommandMap";
      final MethodType type = MethodType.methodType(SimpleCommandMap.class);
      SIMPLE_COMMAND_MAP =
          (SimpleCommandMap)
              MethodHandles.publicLookup()
                  .findVirtual(clazz, name, type)
                  .invoke(Bukkit.getServer());
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull SimpleCommandMap getCommandMap() {
    return SIMPLE_COMMAND_MAP;
  }
}
