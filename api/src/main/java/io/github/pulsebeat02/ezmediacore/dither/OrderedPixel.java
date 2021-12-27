package io.github.pulsebeat02.ezmediacore.dither;

public final class OrderedPixel {

  private final int r;
  private final int g;
  private final int b;

  private OrderedPixel(final int r, final int g, final int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public int getR() {
    return this.r;
  }

  public int getG() {
    return this.g;
  }

  public int getB() {
    return this.b;
  }

  public static OrderedPixel ofPixel(final int r, final int g, final int b) {
    return new OrderedPixel(r, g, b);
  }
}
