/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered;

public final class OrderedPixelMapper {

  private final float[][] matrix;

  private OrderedPixelMapper(final int[][] matrix, final int max, final float strength) {
    this.matrix = this.calculateMatrixArray(matrix, max, strength);
  }

  public static OrderedPixelMapper ofPixelMapper(
      final int[][] matrix, final int max, final float strength) {
    return new OrderedPixelMapper(matrix, max, strength);
  }

  private float convertThresholdToAddition(final float scale, final int value, final int max) {
    return (float) (scale * ((value + 1.0) / max - 0.50000006));
  }

  private float[]  [] calculateMatrixArray(
      final int[]  [] matrix, final int max, final float strength) {
    final int ydim = matrix.length;
    final int xdim = matrix[0].length;
    final float scale = 65535.0f * strength;
    final float[][] precalc = new float[ydim][xdim];
    for (int i = 0; i < ydim; i++) {
      for (int j = 0; j < xdim; j++) {
        precalc[i][j] = this.convertThresholdToAddition(scale, matrix[i][j], max);
      }
    }
    return precalc;
  }

  public float[][] getMatrix() {
    return this.matrix;
  }
}
