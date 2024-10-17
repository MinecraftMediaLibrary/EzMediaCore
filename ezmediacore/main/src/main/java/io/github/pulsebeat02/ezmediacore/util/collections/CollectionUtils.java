package io.github.pulsebeat02.ezmediacore.util.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CollectionUtils {

  private CollectionUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T[] mapToArray(final Map<T, T> map) {
    final int size = map.size();
    final Class<?> clazz = map.getClass();
    final Class<?> componentType = clazz.getComponentType();
    final T[] array = (T[]) Array.newInstance(componentType, size);
    int index = 0;
    final Set<Map.Entry<T, T>> entries = map.entrySet();
    for (final Map.Entry<T, T> entry : entries) {
      array[index++] = entry.getKey();
      array[index++] = entry.getValue();
    }
    return array;
  }

  @SafeVarargs
  public static <T> T[] merge(final T[]... arrays) {

    int totalLength = 0;
    for (final T[] array : arrays) {
      totalLength += array.length;
    }

    final T[] result = Arrays.copyOf(arrays[0], totalLength);
    int offset = arrays[0].length;

    for (int i = 1; i < arrays.length; i++) {
      System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
      offset += arrays[i].length;
    }

    return result;
  }

  @SafeVarargs
  public static <K, V> Map<K, V> merge(final Map<K, V>... maps) {
    final Map<K, V> result = new HashMap<>();
    for (final Map<K, V> map : maps) {
      result.putAll(map);
    }
    return result;
  }
}
