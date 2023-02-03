package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AudioPlayback {
  RESOURCEPACK,
  HTTP,
  DISCORD;

  private static final Map<String, AudioPlayback> KEYS;

  static {
    KEYS = new HashMap<>();
    for (final AudioPlayback type : AudioPlayback.values()) {
      KEYS.put(type.name(), type);
    }
  }

  public static @NotNull Optional<AudioPlayback> ofKey(@NotNull final String key) {
    return Optional.ofNullable(KEYS.get(key));
  }
}
