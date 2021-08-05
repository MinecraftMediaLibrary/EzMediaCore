package io.github.pulsebeat02.ezmediacore.utility;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class CollectionUtils {

  private CollectionUtils() {
  }

  @NotNull
  public static <K, V> Multimap<K, V> createMultiMap(final Map<K, ? extends Iterable<V>> input) {
    final Multimap<K, V> multimap = ArrayListMultimap.create();
    for (final Map.Entry<K, ? extends Iterable<V>> entry : input.entrySet()) {
      multimap.putAll(entry.getKey(), entry.getValue());
    }
    return multimap;
  }
}
