package io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered;

@FunctionalInterface
public interface OrderedDitherFunction {

  void dither(
      final int[] data,
      final int width,
      final int x,
      final int y,
      final int r,
      final int g,
      final int b);
}
