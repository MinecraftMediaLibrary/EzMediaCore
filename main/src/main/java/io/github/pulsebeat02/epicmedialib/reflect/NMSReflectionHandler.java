package io.github.pulsebeat02.epicmedialib.reflect;

import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.nms.PacketHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class NMSReflectionHandler {

  public static final String VERSION;

  static {
    VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }

  @NotNull
  public static Optional<PacketHandler> getNewPacketHandlerInstance() {
    try {
      Logger.info(String.format("Loading NMS Class for Version %s", VERSION));
      final Class<?> clazz =
          Class.forName(
              String.format(
                  "io.github.pulsebeat02.epicmedialib.nms.impl.%s.NMSMapPacketIntercepter",
                  VERSION));
      return Optional.of((PacketHandler) clazz.getDeclaredConstructor().newInstance());
    } catch (final ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      Logger.error(
          String.format(
              "The Server Version you are using (%s) is not yet supported by EpicMediaLib! Shutting down due to the Fatal Error",
              VERSION));
      return Optional.empty();
    }
  }
}
