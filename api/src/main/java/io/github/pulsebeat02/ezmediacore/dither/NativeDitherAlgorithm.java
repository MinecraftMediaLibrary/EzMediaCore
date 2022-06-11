package io.github.pulsebeat02.ezmediacore.dither;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import org.jetbrains.annotations.NotNull;

public interface NativeDitherAlgorithm extends DitherAlgorithm {

  @NotNull
  BufferCarrier ditherIntoMinecraftNatively(final int @NotNull [] buffer, final int width);
}
