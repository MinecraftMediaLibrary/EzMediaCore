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

import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_4X4;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_4X4_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_16X16;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_16X16_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_6X6;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_6X6_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_HORIZONTAL_LINE;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_HORIZONTAL_LINE_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_SPIRAL_5X5;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_SPIRAL_5X5_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_VERTICAL_LINE;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_VERTICAL_LINE_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.HORIZONTAL_3X5;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.HORIZONTAL_3X5_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_2X2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_2X2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_4X4;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_4X4_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.VERTICAL_5X3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.VERTICAL_5X3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.createBayerMatrix;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FilterLiteDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FloydDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedPixelMapper;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.random.RandomDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.simple.SimpleDither;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum DitherSetting {
  FILTER_LITE("Filter Lite", new FilterLiteDither()),
  FLOYD_STEINBERG("Floyd Steinberg", new FloydDither()),

  BAYER_2X2("Bayer 2x2", ordered(NORMAL_2X2, NORMAL_2X2_MAX)),
  BAYER_4X4("Bayer 4x4", ordered(NORMAL_4X4, NORMAL_4X4_MAX)),
  BAYER_8X8("Bayer 8x8", ordered(NORMAL_8X8, NORMAL_8X8_MAX)),
  BAYER_16X16("Bayer 16x16", ordered(createBayerMatrix(16, 16), 16 * 16)),
  BAYER_32X32("Bayer 32x32", ordered(createBayerMatrix(32, 32), 32 * 32)),
  BAYER_64X64("Bayer 64x64", ordered(createBayerMatrix(64, 64), 64 * 64)),
  BAYER_CLUSTERED_DOT_4X4(
      "Bayer Clustered Dot 4x4", ordered(CLUSTERED_DOT_4X4, CLUSTERED_DOT_4X4_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8(
      "Bayer Clustered Dot Diagonal 8x8",
      ordered(CLUSTERED_DOT_DIAGONAL_8X8, CLUSTERED_DOT_DIAGONAL_8X8_MAX)),
  BAYER_VERTICAL_5X3("Bayer Vertical 5x3", ordered(VERTICAL_5X3, VERTICAL_5X3_MAX)),
  BAYER_HORIZONTAL_3X5("Bayer Horizontal 3x5", ordered(HORIZONTAL_3X5, HORIZONTAL_3X5_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_6X6(
      "Bayer Clustered Dot Diagonal 6x6",
      ordered(CLUSTERED_DOT_DIAGONAL_6X6, CLUSTERED_DOT_DIAGONAL_6X6_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8_2(
      "Bayer Clustered Dot Diagonal 6x6 (v2)",
      ordered(CLUSTERED_DOT_DIAGONAL_8X8_2, CLUSTERED_DOT_DIAGONAL_8X8_2_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_16X16(
      "Bayer Clustered Dot Diagonal 16x16",
      ordered(CLUSTERED_DOT_DIAGONAL_16X16, CLUSTERED_DOT_DIAGONAL_16X16_MAX)),
  BAYER_CLUSTERED_DOT_6X6(
      "Bayer Clustered Dot 6x6", ordered(CLUSTERED_DOT_6X6, CLUSTERED_DOT_6X6_MAX)),
  BAYER_CLUSTERED_DOT_SPIRAL_5X5(
      "Bayer Clustered Dot Spiral 5x5",
      ordered(CLUSTERED_DOT_SPIRAL_5X5, CLUSTERED_DOT_SPIRAL_5X5_MAX)),
  BAYER_CLUSTERED_DOT_HORIZONTAL_LINE(
      "Bayer Clustered Dot Horizontal Line",
      ordered(CLUSTERED_DOT_HORIZONTAL_LINE, CLUSTERED_DOT_HORIZONTAL_LINE_MAX)),
  BAYER_CLUSTERED_DOT_VERTICAL_LINE(
      "Bayer Clustered Dot Vertical Line",
      ordered(CLUSTERED_DOT_VERTICAL_LINE, CLUSTERED_DOT_VERTICAL_LINE_MAX)),
  BAYER_CLUSTERED_DOT_8X8(
      "Bayer Clustered Dot 8x8", ordered(CLUSTERED_DOT_8X8, CLUSTERED_DOT_8X8_MAX)),
  BAYER_CLUSTERED_DOT_6X6_2(
      "Bayer Clustered Dot 6x6 (v2)", ordered(CLUSTERED_DOT_6X6_2, CLUSTERED_DOT_6X6_2_MAX)),
  BAYER_CLUSTERED_DOT_6X6_3(
      "Bayer Clustered Dot 6x6 (v3)", ordered(CLUSTERED_DOT_6X6_3, CLUSTERED_DOT_6X6_3_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8_3(
      "Bayer Clustered Dot 8x8 (v3)",
      ordered(CLUSTERED_DOT_DIAGONAL_8X8_3, CLUSTERED_DOT_DIAGONAL_8X8_3_MAX)),

  RANDOM_DITHER_LIGHT("Random Dithering (Light)", new RandomDither(RandomDither.LIGHT_WEIGHT)),
  RANDOM_DITHER_NORMAL("Random Dithering (Normal)", new RandomDither(RandomDither.NORMAL_WEIGHT)),
  RANDOM_DITHER_HEAVY("Random Dithering (Heavy)", new RandomDither(RandomDither.HEAVY_WEIGHT)),

  SIMPLE_DITHER("Standard Dithering", new SimpleDither());

  private static final Map<String, DitherSetting> maps;

  static {
    maps = new HashMap<>();
    for (final DitherSetting setting : DitherSetting.values()) {
      maps.put(setting.name(), setting);
    }
  }

  private static OrderedDither ordered(final int[][] matrix, final int max) {
    return new OrderedDither(OrderedPixelMapper.ofPixelMapper(matrix, max, 0.005f));
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
