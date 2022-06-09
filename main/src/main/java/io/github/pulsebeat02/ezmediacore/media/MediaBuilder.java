package io.github.pulsebeat02.ezmediacore.media;

import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MediaBuilder {

  MediaBuilder() {}

  @Contract(value = " -> new", pure = true)
  public static @NotNull MediaBuilder builder() {
    return new MediaBuilder();
  }

  @Contract("_ -> this")
  public @NotNull MediaBuilder player(@NotNull final MediaPlayer player) {
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MediaBuilder audioStrategy(@NotNull final AudioStrategy strategy) {
    return this;
  }
}
