package io.github.pulsebeat02.deluxemediaplugin.command.dither;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public enum DitherSetting {
  FILTER_LITE("Filter Lite"),
  FLOYD_STEINBERG("Floyd Steinberg"),
  ORDERED_DITHER_2("Ordered Matrix (2x2)"),
  ORDERED_DITHER_4("Ordered Matrix (4x4)"),
  ORDERED_DITHER_8("Ordered Matrix (8x8)"),
  SIMPLE_DITHER("Standard Dithering");

  private static final Map<String, DitherSetting> maps;

  static {
    maps = new HashMap<>();
    for (final DitherSetting setting : DitherSetting.values()) {
      maps.put(setting.name, setting);
    }
  }

  private final String name;

  DitherSetting(@NotNull final String name) {
    this.name = name;
  }

  public static DitherSetting fromString(@NotNull final String key) {
    return maps.get(key);
  }

  public String getName() {
    return this.name;
  }
}
