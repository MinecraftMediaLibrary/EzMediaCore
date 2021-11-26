package io.github.pulsebeat02.deluxemediaplugin.utility.nullability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Nill {

  private Nill() {}

  public static <T> void ifNot(@Nullable final T obj, @NotNull final Runnable runnable) {
    if (!isNull(obj)) {
      runnable.run();
    }
  }

  public static <T> void ifSo(@Nullable final T obj, @NotNull final Runnable runnable) {
    if (isNull(obj)) {
      runnable.run();
    }
  }

  private static <T> boolean isNull(@Nullable final T obj) {
    return obj == null;
  }
}
