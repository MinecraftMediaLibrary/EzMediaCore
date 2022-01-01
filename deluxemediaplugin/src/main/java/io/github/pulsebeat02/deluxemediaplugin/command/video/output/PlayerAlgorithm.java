package io.github.pulsebeat02.deluxemediaplugin.command.video.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum PlayerAlgorithm {
  VLC,
  FFMPEG,
  JCODEC,
  UNSPECIFIED;

  private static final Map<String, PlayerAlgorithm> KEY_LOOKUP;

  static {
    KEY_LOOKUP = new HashMap<>();
    for (final PlayerAlgorithm setting : PlayerAlgorithm.values()) {
      KEY_LOOKUP.put(setting.name(), setting);
    }
  }

  public static @NotNull Optional<PlayerAlgorithm> ofKey(@NotNull final String key) {
    return Optional.ofNullable(KEY_LOOKUP.get(key));
  }
}
