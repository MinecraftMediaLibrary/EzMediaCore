package io.github.pulsebeat02.ezmediacore.utility;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

public class ImmutableDimension implements ConfigurationSerializable {

  private final int width;
  private final int height;

  ImmutableDimension(final int width, final int height) {
    this.width = width;
    this.height = height;
  }

  public static ImmutableDimension of(final int width, final int height) {
    return new ImmutableDimension(width, height);
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  @Override
  public @NotNull Map<String, Object> serialize() {
    return ImmutableMap.of(
        "width", this.width,
        "height", this.height);
  }
}