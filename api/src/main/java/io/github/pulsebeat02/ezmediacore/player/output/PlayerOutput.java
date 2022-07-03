package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.NotNull;

public interface PlayerOutput<T> {

  @NotNull
  T getResultingOutput();
}
