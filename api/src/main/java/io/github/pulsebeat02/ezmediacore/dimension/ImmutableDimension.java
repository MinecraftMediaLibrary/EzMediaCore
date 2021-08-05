package io.github.pulsebeat02.ezmediacore.dimension;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

public record ImmutableDimension(int width, int height) implements ConfigurationSerializable {

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  @Override
  public @NotNull
  Map<String, Object> serialize() {
    return ImmutableMap.of(
        "width", this.width,
        "height", this.height);
  }
}
