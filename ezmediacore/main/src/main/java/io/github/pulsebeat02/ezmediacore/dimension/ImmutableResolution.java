package io.github.pulsebeat02.ezmediacore.dimension;

public final class ImmutableResolution implements Resolution {

  private final int width;
  private final int height;

  public ImmutableResolution(final int width, final int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }
}
