package io.github.pulsebeat02.deluxemediaplugin.command.dither;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FilterLiteDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FloydDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.OrderedDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.SimpleDither;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public enum DitherSetting {
  FILTER_LITE("Filter Lite", new FilterLiteDither()),
  FLOYD_STEINBERG("Floyd Steinberg", new FloydDither()),
  ORDERED_DITHER_2("Ordered Matrix (2x2)", new OrderedDither(OrderedDither.DitherType.TWO)),
  ORDERED_DITHER_4("Ordered Matrix (4x4)", new OrderedDither(OrderedDither.DitherType.FOUR)),
  ORDERED_DITHER_8("Ordered Matrix (8x8)", new OrderedDither(OrderedDither.DitherType.EIGHT)),
  SIMPLE_DITHER("Standard Dithering", new SimpleDither());

  private static final Map<String, DitherSetting> maps;

  static {
    maps = new HashMap<>();
    for (final DitherSetting setting : DitherSetting.values()) {
      maps.put(setting.name, setting);
    }
  }

  private final String name;
  private final DitherAlgorithm algorithm;

  DitherSetting(@NotNull final String name, @NotNull final DitherAlgorithm algorithm) {
    this.name = name;
    this.algorithm = algorithm;
  }

  public static DitherSetting fromString(@NotNull final String key) {
    return maps.get(key);
  }

  public String getName() {
    return this.name;
  }

  public DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }
}
