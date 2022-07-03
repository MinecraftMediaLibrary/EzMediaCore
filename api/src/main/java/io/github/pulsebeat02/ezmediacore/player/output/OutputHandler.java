package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.NotNull;

public interface OutputHandler<T> {

  @NotNull
  T getRaw();
}
