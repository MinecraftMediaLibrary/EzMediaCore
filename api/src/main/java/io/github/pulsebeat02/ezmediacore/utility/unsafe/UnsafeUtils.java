package io.github.pulsebeat02.ezmediacore.utility.unsafe;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

public final class UnsafeUtils {

  private static final Unsafe UNSAFE;

  static {
    UNSAFE = UnsafeProvider.getUnsafe();
  }

  private UnsafeUtils() {}

  /**
   * Sets a specific final field for a class (not static!).
   *
   * @param field the final field
   * @param obj the object to set the final field on
   * @param value the value
   */
  public static void setFinalField(
      @NotNull final Field field, @NotNull final Object obj, @Nullable final Object value) {
    UNSAFE.putObject(obj, UNSAFE.objectFieldOffset(field), value);
  }

  /**
   * Sets a specific static final field for a class.
   *
   * @param field the static final field
   * @param value the value
   */
  public static void setStaticFinalField(@NotNull final Field field, @Nullable final Object value) {
    UNSAFE.putObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field), value);
  }

  public static @NotNull Object getFieldExceptionally(
      @NotNull final Object object, @NotNull final String name) {
    try {
      return getField(object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull Object getFieldExceptionally(
      @NotNull final Class<?> clazz, @NotNull final Object object, @NotNull final String name) {
    try {
      return getField(clazz, object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull Object getField(@NotNull final Object object, @NotNull final String name)
      throws NoSuchFieldException {
    return getField(object.getClass(), object, name);
  }

  public static @NotNull Object getField(
      @NotNull final Class<?> clazz, @NotNull final Object object, @NotNull final String name)
      throws NoSuchFieldException {
    return UNSAFE.getObject(object, UNSAFE.objectFieldOffset(clazz.getDeclaredField(name)));
  }
}
