package io.github.pulsebeat02.ezmediacore.utility.misc;

import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class Try {

  private Try() {}

  public static void closeable(@NotNull final AutoCloseable closeable) {
    try {
      closeable.close();
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  public static void sleep(@NotNull final TimeUnit unit, final long duration) {
    try {
      unit.sleep(duration);
    } catch (final InterruptedException e) {
      throw new AssertionError(e);
    }
  }
}
