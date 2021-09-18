package io.github.pulsebeat02.ezmediacore.player;

import org.jetbrains.annotations.NotNull;

public interface BufferedPlayer {

  void setBufferConfiguration(@NotNull final BufferConfiguration configuration);

  @NotNull
  BufferConfiguration getBufferConfiguration();
}
