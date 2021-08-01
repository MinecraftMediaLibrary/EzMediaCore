package io.github.pulsebeat02.epicmedialib.sneaky;

import org.jetbrains.annotations.NotNull;

public final class SneakyThrowUtil {

  private SneakyThrowUtil() {}

  static <T extends Exception, R> R sneakyThrow(@NotNull final Exception t) throws T {
    throw (T) t;
  }
}
