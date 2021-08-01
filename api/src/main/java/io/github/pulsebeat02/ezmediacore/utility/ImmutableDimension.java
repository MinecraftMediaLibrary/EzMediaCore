package io.github.pulsebeat02.ezmediacore.utility;

public class ImmutableDimension {

  private final int width;
  private final int height;

  public ImmutableDimension(final int width, final int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }
}
