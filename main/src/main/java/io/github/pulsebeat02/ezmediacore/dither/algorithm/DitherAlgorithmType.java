package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.OrderedDither.DitherType;
import org.jetbrains.annotations.NotNull;

public enum DitherAlgorithmType {
  FILTER_LITE(new FilterLiteDither()),
  FLOYD_STEINBERG(new FloydDither()),
  ORDERED_2X2(new OrderedDither(DitherType.TWO)),
  ORDERED_4X4(new OrderedDither(DitherType.FOUR)),
  ORDERED_8X8(new OrderedDither(DitherType.EIGHT)),
  RANDOM(new RandomDither()),
  SIMPLE(new SimpleDither());

  private final DitherAlgorithm algorithm;

  DitherAlgorithmType(@NotNull final DitherAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

  public DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }
}
