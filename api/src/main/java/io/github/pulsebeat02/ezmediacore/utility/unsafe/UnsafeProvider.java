package io.github.pulsebeat02.ezmediacore.utility.unsafe;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public final class UnsafeProvider {

  private static final Unsafe UNSAFE;

  static {
    try {
      final Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe) field.get(null);
    } catch (final IllegalAccessException | NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  private UnsafeProvider() {}

  public static @NotNull Unsafe getUnsafe() {
    return UNSAFE;
  }
}
