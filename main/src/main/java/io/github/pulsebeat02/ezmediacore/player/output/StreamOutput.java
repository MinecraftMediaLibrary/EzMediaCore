package io.github.pulsebeat02.ezmediacore.player.output;

import java.io.InputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StreamOutput implements OutputHandler<InputStream> {

  private final InputStream stream;

  StreamOutput(@NotNull final InputStream stream) {
    this.stream = stream;
  }

  @Contract("_ -> new")
  public static @NotNull StreamOutput ofStream(@NotNull final InputStream stream) {
    return new StreamOutput(stream);
  }

  @Override
  public @NotNull InputStream getRaw() {
    return this.stream;
  }
}
