package io.github.pulsebeat02.ezmediacore.dimension;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ImmutableDimension implements Dimension {

  private final int width;
  private final int height;

  public ImmutableDimension(final int width, final int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public @NotNull
  Map<String, Object> serialize() {
    return ImmutableMap.of(
        "width", this.width,
        "height", this.height);
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
