package io.github.pulsebeat02.ezmediacore.player;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public interface ConsumablePlayer {

  void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels);

  void setCustomAudioAdapter(
      @NotNull final Consumer<byte[]> audio,
      final int blockSize,
      @NotNull final String format,
      final int rate,
      final int channels);
}
