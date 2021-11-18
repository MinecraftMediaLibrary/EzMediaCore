package io.github.pulsebeat02.ezmediacore.utility.unsafe;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public final class UnsafeUtils {

  private UnsafeUtils() {}

  private static final Unsafe UNSAFE;

  static {
    UNSAFE = UnsafeManager.getUnsafe();
  }

  /**
   * Sets a specific final field for a class (not static!).
   *
   * @param field the final field
   * @param obj the object to set the final field on
   * @param value the value
   */
  public static void setFinalField(
      @NotNull final Field field, @NotNull final Object obj, @NotNull final Object value) {
    UNSAFE.putObject(obj, UNSAFE.objectFieldOffset(field), value);
  }

  /**
   * Sets a specific static final field for a class.
   *
   * @param field the static final field
   * @param value the value
   */
  public static void setStaticFinalField(@NotNull final Field field, @NotNull final Object value) {
    UNSAFE.putObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field), value);
  }
}
