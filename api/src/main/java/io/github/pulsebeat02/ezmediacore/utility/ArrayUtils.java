package io.github.pulsebeat02.ezmediacore.utility;

import java.lang.reflect.Array;
import org.jetbrains.annotations.NotNull;

public final class ArrayUtils {

  private ArrayUtils() {}

  // 2 ... 5
  // 2 3 4
  public static <T> T @NotNull [] trimOneDimensionalArray(
      final T @NotNull [] array, final int startInclusive, final int endExclusive) {
    final T[] trimmed =
        (T[]) Array.newInstance(array.getClass().getComponentType(), endExclusive - startInclusive);
    int index = 0;
    for (int i = startInclusive; i < endExclusive; i++, index++) {
      trimmed[index] = array[i];
    }
    return trimmed;
  }
}
