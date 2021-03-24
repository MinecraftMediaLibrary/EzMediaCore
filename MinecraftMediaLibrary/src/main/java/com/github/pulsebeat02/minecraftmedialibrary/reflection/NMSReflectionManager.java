/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.reflection;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * The special reflection class used to fetch the correct reflection class for each server version.
 * It is possible to use a big switch statement, however, I used reflection for conciseness and not
 * having to deal with classes that are the same name.
 */
public class NMSReflectionManager {

  /** The constant VERSION. */
  public static final String VERSION;

  static {
    VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }

  /**
   * Gets new packet handler instance.
   *
   * @param library the library
   * @return the new packet handler instance
   */
  public static PacketHandler getNewPacketHandlerInstance(
      @NotNull final MinecraftMediaLibrary library) {
    try {
      Logger.info("Loading NMS Class for Version " + VERSION);
      final Class<?> clazz =
          Class.forName(
              "com.github.pulsebeat02.minecraftmedialibrary.nms.impl."
                  + VERSION
                  + ".NMSMapPacketIntercepter");
      return (PacketHandler) clazz.getDeclaredConstructor().newInstance();
    } catch (final ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      Logger.error(
          "The Server Version you are using ("
              + VERSION
              + ") is not yet supported by MinecraftMediaLibrary! "
              + "Shutting down due to the Fatal Error");
      library.shutdown();
      return null;
    }
  }
}
