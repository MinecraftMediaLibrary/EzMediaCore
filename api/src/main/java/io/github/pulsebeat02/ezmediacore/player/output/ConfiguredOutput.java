package io.github.pulsebeat02.ezmediacore.player.output;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ConfiguredOutput {

  void setProperty(@NotNull final String key, @NotNull final String value);

  @NotNull Map<String, String> getConfiguration();
}
