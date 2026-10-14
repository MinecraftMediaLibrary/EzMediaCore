/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.ordered;

import com.google.common.base.Preconditions;

/**
 * The default {@link PixelMapper}, which normalizes an integer threshold matrix.
 *
 * <p>Every entry is mapped to the range from -0.5 to 0.5 based on its rank in the matrix, and multiplied by the
 * strength. The minimum and maximum of the matrix are taken from the matrix itself, so matrices that count from
 * zero and matrices that count from one both work.
 */
public final class OrderedPixelMapper implements PixelMapper {

  private static final String ROW_NULL_MESSAGE = "Threshold matrix rows must not be null!";

  private static final double CENTER_OFFSET = 0.5;

  private final float[][] matrix;
  private final float strength;

  OrderedPixelMapper(final int[][] thresholds, final float strength) {
    Preconditions.checkNotNull(thresholds, "Thresholds must not be null!");
    Preconditions.checkArgument(thresholds.length > 0, "Threshold matrix must have at least one row!");

    final boolean finite = Float.isFinite(strength);
    Preconditions.checkArgument(finite && strength >= 0, "Strength must be finite and not negative!");

    this.strength = strength;
    this.matrix = normalize(thresholds, strength);
  }

  private static float[][] normalize(final int[][] thresholds, final float strength) {
    final int columns = checkRectangular(thresholds);
    final int minimum = findMinimum(thresholds);
    final int maximum = findMaximum(thresholds);
    final long levels = (long) maximum - minimum + 1;

    final float[][] normalized = new float[thresholds.length][columns];
    for (int row = 0; row < thresholds.length; row++) {
      for (int column = 0; column < columns; column++) {
        final long rank = (long) thresholds[row][column] - minimum;
        final float centered = (float) ((rank + CENTER_OFFSET) / levels - CENTER_OFFSET);
        normalized[row][column] = centered * strength;
      }
    }

    return normalized;
  }

  private static int checkRectangular(final int[][] thresholds) {
    final int[] firstRow = thresholds[0];
    Preconditions.checkNotNull(firstRow, ROW_NULL_MESSAGE);

    final int columns = firstRow.length;
    Preconditions.checkArgument(columns > 0, "Threshold matrix must have at least one column!");

    for (final int[] row : thresholds) {
      Preconditions.checkNotNull(row, ROW_NULL_MESSAGE);
      Preconditions.checkArgument(row.length == columns, "Threshold matrix rows must have the same length!");
    }

    return columns;
  }

  private static int findMinimum(final int[][] thresholds) {
    int minimum = Integer.MAX_VALUE;
    for (final int[] row : thresholds) {
      for (final int value : row) {
        minimum = Math.min(minimum, value);
      }
    }

    return minimum;
  }

  private static int findMaximum(final int[][] thresholds) {
    int maximum = Integer.MIN_VALUE;
    for (final int[] row : thresholds) {
      for (final int value : row) {
        maximum = Math.max(maximum, value);
      }
    }

    return maximum;
  }

  /**
   * Returns the normalized threshold pattern, indexed by row and then column, with every entry from -0.5 to 0.5 times
   * the strength.
   *
   * @return the pattern, which is shared and must not be modified
   */
  @Override
  public float[][] getMatrix() {
    return this.matrix;
  }

  /**
   * Returns the strength the pattern was scaled by.
   *
   * @return the strength
   */
  @Override
  public float getStrength() {
    return this.strength;
  }
}
