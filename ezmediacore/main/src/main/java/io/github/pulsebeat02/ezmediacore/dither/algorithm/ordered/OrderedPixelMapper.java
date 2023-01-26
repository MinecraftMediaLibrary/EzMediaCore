package io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class OrderedPixelMapper {

  private final float[][] matrix;

  private OrderedPixelMapper(final int[][] matrix, final int max, final float strength) {
    this.matrix = this.calculateMatrixArray(matrix, max, strength);
  }

  @Contract("_, _, _ -> new")
  public static @NotNull OrderedPixelMapper ofPixelMapper(
      final int[][] matrix, final int max, final float strength) {
    return new OrderedPixelMapper(matrix, max, strength);
  }

  private float convertThresholdToAddition(final float scale, final int value, final int max) {
    return (float) (scale * ((value + 1.0) / max - 0.50000006));
  }

  private float[] @NotNull [] calculateMatrixArray(
      final int[] @NotNull [] matrix, final int max, final float strength) {
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
