package io.github.pulsebeat02.deluxemediaplugin.utility;

import org.jetbrains.annotations.NotNull;

public final class Pair<K, V> {

  private final K key;
  private final V value;

  public Pair(@NotNull final K key, @NotNull final V value) {
    this.key = key;
    this.value = value;
  }

  public @NotNull K getKey() {
    return this.key;
  }

  public @NotNull V getValue() {
    return this.value;
  }
}
