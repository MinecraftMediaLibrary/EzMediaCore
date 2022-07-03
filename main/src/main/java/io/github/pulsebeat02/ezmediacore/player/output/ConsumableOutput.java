package io.github.pulsebeat02.ezmediacore.player.output;

import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrame;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ConsumableOutput implements OutputHandler<VLCFrame> {

  private final VLCFrame frame;

  ConsumableOutput(@NotNull final VLCFrame frame) {
    this.frame = frame;
  }

  @Contract("_ -> new")
  public static @NotNull ConsumableOutput ofFrame(@NotNull final VLCFrame frame) {
    return new ConsumableOutput(frame);
  }

  @Override
  public @NotNull VLCFrame getRaw() {
    return this.frame;
  }
}
