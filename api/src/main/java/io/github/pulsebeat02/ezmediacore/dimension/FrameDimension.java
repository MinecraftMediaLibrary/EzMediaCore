package io.github.pulsebeat02.ezmediacore.dimension;

import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.of;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.square;

public final class FrameDimension {

  public static final Dimension X1_1;
  public static final Dimension X1_2;
  public static final Dimension X3_3;
  public static final Dimension X3_5;
  public static final Dimension X5_5;
  public static final Dimension X6_10;

  public static final Dimension X8_14;
  public static final Dimension X8_18;
  public static final Dimension X10_14;

  static {
    X1_1 = square(1);
    X1_2 = of(1, 2);
    X3_3 = square(3);
    X3_5 = of(3, 5);
    X5_5 = square(5);
    X6_10 = of(6, 10);

    X8_14 = of(8, 14);
    X8_18 = of(8, 18);
    X10_14 = of(10, 14);
  }

  private FrameDimension() {
  }

}
