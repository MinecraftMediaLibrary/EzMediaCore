/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.deluxemediaplugin.command.dither;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FilterLiteDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FloydDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.OrderedDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.SimpleDither;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
      maps.put(setting.name(), setting);
    }
  }

  private final String name;
  private final DitherAlgorithm algorithm;

  DitherSetting(@NotNull final String name, @NotNull final DitherAlgorithm algorithm) {
    this.name = name;
    this.algorithm = algorithm;
  }

  public static @NotNull Optional<DitherSetting> ofKey(@NotNull final String key) {
    return Optional.ofNullable(maps.get(key));
  }

  public @NotNull String getName() {
    return this.name;
  }

  public @NotNull DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }
}
