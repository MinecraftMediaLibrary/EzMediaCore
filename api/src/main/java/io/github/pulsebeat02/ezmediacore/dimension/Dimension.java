package io.github.pulsebeat02.ezmediacore.dimension;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

public interface Dimension extends ConfigurationSerializable {

  static Dimension of(final int width, final int height) {
    return new ImmutableDimension(width, height);
  }

  static Dimension square(final int side) {
    return of(side, side);
  }

  static Dimension inverse(@NotNull final Dimension dimension) {
    return of(dimension.getHeight(), dimension.getWidth());
  }

  int getWidth();

  int getHeight();

}
