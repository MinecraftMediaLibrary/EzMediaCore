package io.github.pulsebeat02.deluxemediaplugin.command.video;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum AudioOutputType {
  RESOURCEPACK,
  DISCORD;

  private static final Map<String, AudioOutputType> KEYS;

  static {
    KEYS =
        Map.of(
            "RESOURCEPACK", RESOURCEPACK,
            "DISCORD", DISCORD);
  }

  public static @NotNull Optional<AudioOutputType> ofKey(@NotNull final String key) {
    return Optional.ofNullable(KEYS.get(key));
  }
}
