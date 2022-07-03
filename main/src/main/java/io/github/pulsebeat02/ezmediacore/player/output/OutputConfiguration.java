package io.github.pulsebeat02.ezmediacore.player.output;

import com.google.common.collect.Maps;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class OutputConfiguration implements ConfiguredOutput {

  private final Map<String, String> configuration;

  public OutputConfiguration() {
    this.configuration = Maps.newHashMap();
  }

  @Override
  public void setProperty(@NotNull final String key, @NotNull final String value) {
    this.configuration.put(key, value);
  }

  @Override
  public @NotNull Map<String, String> getConfiguration() {
    return this.configuration;
  }
}
