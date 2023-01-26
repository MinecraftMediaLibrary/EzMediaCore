package io.github.pulsebeat02.ezmediacore.player.output;

import com.google.common.collect.Maps;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class OutputConfiguration implements ConfiguredOutput {

  private final Map<String, String> configuration;

  public OutputConfiguration() {
    this.configuration = Maps.newHashMap();
  }

  public OutputConfiguration(@NotNull final Map<String, String> configuration) {
    this.configuration = Maps.newHashMap(configuration);
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
