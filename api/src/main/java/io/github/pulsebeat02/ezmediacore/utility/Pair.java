package io.github.pulsebeat02.ezmediacore.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Pair<K, V> {

  private final K key;
  private final V value;

  Pair(@NotNull final K key, @NotNull final V value) {
    this.key = key;
    this.value = value;
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static <K, V> @NotNull Pair<K, V> ofPair(@NotNull final K key, @NotNull final V value) {
    return new Pair<>(key, value);
  }

  public @NotNull K getKey() {
    return this.key;
  }

  public @NotNull V getValue() {
    return this.value;
  }
}
