package io.github.pulsebeat02.ezmediacore.dither;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface DitherAlgorithm {

  ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width);

  default void dither(final int[] buffer, final int width) {
  }
}
