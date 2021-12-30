package io.github.pulsebeat02.ezmediacore.dither;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NativeDitherAlgorithm {

  @NotNull
  BufferCarrier ditherIntoMinecraftNatively(final int @NotNull [] buffer, final int width);

  default void ditherNatively(final int @NotNull [] buffer, final int width) {}
}
