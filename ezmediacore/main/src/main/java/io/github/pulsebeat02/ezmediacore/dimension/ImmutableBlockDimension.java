package io.github.pulsebeat02.ezmediacore.dimension;

public final class ImmutableBlockDimension implements BlockDimension {

  private final int width;
  private final int height;

  public ImmutableBlockDimension(final int width, final int height) {
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
