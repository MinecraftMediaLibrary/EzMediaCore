package io.github.pulsebeat02.ezmediacore.utility.graphics;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.NotNull;

public final class FilterUtils {

  private FilterUtils() {}

  public static @NotNull String getFilterKey(@NotNull final String key) {
    return key.toLowerCase().replaceAll("_", "-");
  }

  public static <T> boolean initializeConstants(@NotNull final Class<T> clazz) {
    final Field[] fields = clazz.getDeclaredFields();
    for (final Field f : fields) {
      if (Modifier.isStatic(f.getModifiers())) {
        try {
          f.setAccessible(true);
          f.set(null, getFilterKey(f.getName()));
        } catch (final IllegalAccessException e) {
          throw new AssertionError(e);
        }
      }
    }
    return true;
  }
}
