package io.github.pulsebeat02.ezmediacore.utility.unsafe;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public final class UnsafeManager {

  private static final Unsafe UNSAFE;

  static {
    try {
      final Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe) field.get(null);
    } catch (final IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
      throw new AssertionError("Could not get Unsafe!");
    }
  }

  private UnsafeManager() {}

  public static @NotNull Unsafe getUnsafe() {
    return UNSAFE;
  }
}
