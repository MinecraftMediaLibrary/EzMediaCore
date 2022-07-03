package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.NotNull;

public final class NullOutput implements OutputHandler<Void> {

  @Override
  public @NotNull Void getRaw() {
    return null;
  }
}
