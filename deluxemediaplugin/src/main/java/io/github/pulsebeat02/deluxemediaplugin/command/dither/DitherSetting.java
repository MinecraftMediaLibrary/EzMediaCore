package io.github.pulsebeat02.deluxemediaplugin.command.dither;

import org.jetbrains.annotations.NotNull;

public enum DitherSetting {
  FILTER_LITE("Filter Lite"),
  FLOYD_STEINBERG("Floyd Steinberg"),
  ORDERED_DITHER_2("Ordered Matrix (2x2)"),
  ORDERED_DITHER_4("Ordered Matrix (4x4)"),
  ORDERED_DITHER_8("Ordered Matrix (8x8)"),
  SIMPLE_DITHER("Standard Dithering");

  private final String name;

  DitherSetting(@NotNull final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
