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
package rewrite.reflect;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import rewrite.logging.Logger;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import rewrite.reflect.versioning.ServerEnvironment;


public final class PacketToolsProvider {

  private static final String CLASS_PATH = "io.github.pulsebeat02.murderrun.reflect.%s.PacketTools";

  private final EzMediaCore core;

  public PacketToolsProvider(final EzMediaCore core) {
    this.core = core;
  }

  private PacketHandler getNewPacketHandlerInstance() {
    try {
      final String version = ServerEnvironment.getNMSRevision();
      final String path = CLASS_PATH.formatted(version);
      final Class<?> clazz = Class.forName(path);
      final MethodHandles.Lookup lookup = MethodHandles.lookup();
      final MethodType type = MethodType.methodType(Void.TYPE);
      final MethodHandle handle = lookup.findConstructor(clazz, type);
      return  (PacketHandler) handle.invoke();
    } catch (final Throwable e) {
      final Server server = Bukkit.getServer();
      final Logger logger = this.core.getLogger();
      final String software = server.getVersion();
      logger.error(Locale.UNSUPPORTED_SERVER.build(software));
      throw new AssertionError(
              "Current server implementation (%s) is not supported!".formatted(software));
    }
  }
}
