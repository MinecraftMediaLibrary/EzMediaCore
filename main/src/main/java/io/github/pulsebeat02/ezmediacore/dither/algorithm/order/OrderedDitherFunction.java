package io.github.pulsebeat02.ezmediacore.dither.algorithm.order;

import io.github.pulsebeat02.ezmediacore.dither.OrderedPixel;

@FunctionalInterface
public interface OrderedDitherFunction {

  OrderedPixel dither(final int x, final int y, final int r, final int g, final int b);

}
