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
package io.github.pulsebeat02.ezmediacore.reflect;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class NMSReflectionHandler {

  private static final String VERSION;

  static {
    VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }

  private final MediaLibraryCore core;

  public NMSReflectionHandler(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  private static @NotNull PacketHandler getPacketHandler()
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
          InstantiationException, IllegalAccessException {
    return (PacketHandler) getPacketHandlerClass().getDeclaredConstructor().newInstance();
  }

  private static @NotNull Class<?> getPacketHandlerClass() throws ClassNotFoundException {
    return Class.forName(
        "io.github.pulsebeat02.ezmediacore.nms.impl.%s.NMSMapPacketInterceptor".formatted(VERSION));
  }

  public static String getVersion() {
    return VERSION;
  }

  public @NotNull PacketHandler getNewPacketHandlerInstance() {
    try {
      return getPacketHandler();
    } catch (final ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      final String software = this.core.getPlugin().getServer().getVersion();
      this.core.getLogger().error(Locale.ERR_SERVER_UNSUPPORTED.build(software));
      throw new AssertionError(
          "Current server implementation (%s) is not supported!".formatted(software));
    }
  }
}
