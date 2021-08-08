package io.github.pulsebeat02.ezmediacore.dimension;

import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.of;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.square;


public final class ChatDimension {

  public static final Dimension X1_1;
  public static final Dimension X2_8;
  public static final Dimension X4_16;
  public static final Dimension X8_16;
  public static final Dimension X8_32;

  public static final Dimension X12_48;
  public static final Dimension X16_48;
  public static final Dimension X16_64;
  public static final Dimension X16_80;
  public static final Dimension X16_92;

  static {
    X1_1 = square(1);
    X2_8 = of(2, 8);
    X4_16 = of(4, 16);
    X8_16 = of(8, 16);
    X8_32 = of(8, 32);

    X12_48 = of(12, 48);
    X16_48 = of(16, 48);
    X16_64 = of(16, 64);
    X16_80 = of(16, 80);
    X16_92 = of(16, 92);
  }

  private ChatDimension() {
  }

}
